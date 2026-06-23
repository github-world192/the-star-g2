先把專案複製下來
想辦法跑起來 你可以使用antigravity (可能要安裝maven)
<img width="1399" height="690" alt="Screenshot 2026-06-23 at 8 58 15 AM" src="https://github.com/user-attachments/assets/43938d27-c21e-439e-a121-f32820d79a62" />
<img width="1399" height="690" alt="Screenshot 2026-06-23 at 8 58 02 AM" src="https://github.com/user-attachments/assets/da2658c2-6b16-4b9c-a8c4-fd1397e2c0a1" />

<img width="1399" height="690" alt="Screenshot 2026-06-23 at 8 57 27 AM" src="https://github.com/user-attachments/assets/12cbcd7d-dbef-4cea-866c-e5e183b67ced" />
流程說明
1. 登入頁：使用者點 Google 按鈕

HomeController 負責 /login 路由，回傳 login.html：

    @GetMapping("/login")
    public String login() {
        return "login";
    }

登入頁上的按鈕連到 Spring Security 內建的 OAuth2 端點：

        <div class="oauth-buttons">
            <a class="btn btn-google" th:href="@{/oauth2/authorization/google}">
                <span class="btn-icon">G</span>
                使用 Google 登入
            </a>

/oauth2/authorization/google 是 Spring Security 自動提供的，不需要自己寫 Controller。

───

2. OAuth2 設定：告訴 Google 怎麼驗證

OAuth2ClientConfig 從 .env 讀取 Google 憑證，註冊 OAuth2 client：

        if (StringUtils.hasText(googleClientId) && StringUtils.hasText(googleClientSecret)) {
            registrations.add(
                ClientRegistration.withRegistrationId("google")
                    .clientId(googleClientId)
                    .clientSecret(googleClientSecret)
                    ...
                    .redirectUri("{baseUrl}" + redirectUri)
                    .scope("email", "profile")
                    ...
                    .userNameAttributeName("sub")

重點：
• registrationId 是 "google"，對應登入頁的 /oauth2/authorization/google
• redirectUri 是 {baseUrl}/callback，本機就是 http://localhost:8081/callback
• scope 要求 email 和 profile
• sub 是 Google 使用者的唯一 ID，之後用來辨識身份

───

3. 安全規則：誰能進、誰要登入

SecurityConfig 是核心，定義了整個登入行為：

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", oauth2RedirectUri, "/error", "/css/**", "/js/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .redirectionEndpoint(redirection -> redirection.baseUri(oauth2RedirectUri))
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .defaultSuccessUrl("/", true)
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

意思是：
┌─────────────────────────────┬────────────────────────────────┐
│ 路徑                        │ 權限                           │
├─────────────────────────────┼────────────────────────────────┤
│ /login、/callback、靜態資源 │ 不需登入                       │
├─────────────────────────────┼────────────────────────────────┤
│ 其他（如 /profile）         │ 必須已登入                     │
├─────────────────────────────┼────────────────────────────────┤
│ 登入成功                    │ 導向 /                         │
├─────────────────────────────┼────────────────────────────────┤
│ 登出                        │ 清 Session，回到 /login?logout │
└─────────────────────────────┴────────────────────────────────┘

───

4. Google 回呼後：建立或更新本地使用者

Google 授權完成後，Spring Security 會呼叫 CustomOAuth2UserService.loadUser()。這是專案自訂的關鍵邏輯：

    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        ...
        String providerId = extractProviderId(provider, attributes);  // Google 的 sub
        String email = extractEmail(provider, attributes, providerId);
        String name = extractName(provider, attributes);
        String avatarUrl = (String) attributes.get("picture");

        userRepository.findByProviderAndProviderId(provider, providerId)
            .map(existing -> { /* 更新頭像 */ })
            .orElseGet(() -> userRepository.save(
                User.builder()
                    .email(email)
                    .name(name)
                    ...
                    .role(UserRole.GUEST)   // 新使用者預設為 GUEST
                    .build()
            ));

        return oauth2User;
    }

邏輯很單純：
1. 先呼叫 super.loadUser() 向 Google 拿使用者資料
2. 用 (provider=GOOGLE, providerId=sub) 查資料庫
3. 已存在 → 若沒頭像就補上
4. 不存在 → 新建一筆 User，角色預設 GUEST
5. 回傳 OAuth2User，Spring Security 據此建立登入 Session

資料庫用 (provider, provider_id) 做唯一約束，避免同一 Google 帳號重複註冊：

@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"})
)

───

5. 登入後：怎麼知道你是誰

登入成功後，Spring Security 會在 Session 裡保存 OAuth2User。之後每個請求 Controller 用 @AuthenticationPrincipal 取得：

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByOAuth2User(principal).orElse(null);

UserService.findByOAuth2User() 用 Google 的 sub 對應到本地 User 資料：

    public Optional<User> findByOAuth2User(OAuth2User principal) {
        ...
        if (attributes.containsKey("sub")) {
            return userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, (String) attributes.get("sub"));
        }
        ...
    }

所以系統有兩層身份：
• OAuth2User（Spring Security Session）：Google 回傳的即時資料
• User（H2 資料庫）：本系統自己的使用者紀錄（角色、電話等）

───

6. 登出

使用者登出時，Spring Security 會：
1. 清除 HTTP Session（invalidateHttpSession）
2. 刪除 JSESSIONID cookie
3. 導回 /login?logout，登入頁顯示「已成功登出」

───

一句話總結

這個專案沒有自己實作帳號密碼登入，而是把身份驗證完全交給 Google；Spring Security 負責 OAuth2 握手與 Session 管理，CustomOAuth2UserService 在 Google 驗證成功後把使用者同步進本地 H2 資料庫，之後就用 sub（Google 唯一 ID）辨
