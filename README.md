# 東方之星飯店管理系統（Hotel System）

這是一個以 **Spring Boot** 打造的飯店管理系統，目前支援 **Google 帳號登入**、個人資料編輯，以及會員首頁。資料庫使用內建的 **H2 記憶體資料庫**，本機開發不需要另外安裝 MySQL 或 PostgreSQL。

跟著這份文件，即使你是第一次接觸 Java 專案，也可以在本機把系統跑起來。

---

## 目錄

- [你需要準備什麼](#你需要準備什麼)
- [第一步：下載專案](#第一步下載專案)
- [第二步：設定 Google 登入（必做）](#第二步設定-google-登入必做)
- [第三步：建立環境變數檔](#第三步建立環境變數檔)
- [第四步：啟動專案](#第四步啟動專案)
- [第五步：打開瀏覽器測試](#第五步打開瀏覽器測試)
- [用 Docker 啟動（可選）](#用-docker-啟動可選)
- [常用指令](#常用指令)
- [專案結構](#專案結構)
- [常見問題](#常見問題)

---

## 你需要準備什麼

在開始之前，請先安裝以下軟體：

| 軟體 | 版本建議 | 用途 | 下載連結 |
|------|----------|------|----------|
| **Java (JDK)** | 21 或以上 | 執行 Spring Boot | [Adoptium Temurin](https://adoptium.net/) |
| **Git** | 最新版 | 下載專案原始碼 | [git-scm.com](https://git-scm.com/) |
| **Docker**（可選） | 最新版 | 用容器方式啟動 | [docker.com](https://www.docker.com/) |

> **小提示：** 專案已內建 Maven Wrapper（`mvnw`），你**不需要**另外安裝 Maven。

### 確認 Java 是否安裝成功

打開終端機（Terminal），輸入：

```bash
java -version
```

如果看到類似 `openjdk version "21.x.x"` 的訊息，代表安裝成功。

---

## 第一步：下載專案

在終端機執行：

```bash
# 複製專案到你的電腦
git clone https://github.com/github-world192/the-star-g2.git
cd hotel-system
```

如果你已經有這份程式碼，只要進入專案資料夾即可：

```bash
cd hotel-system
```

---

## 第二步：設定 Google 登入（必做）

本系統使用 Google OAuth2 登入，你需要到 Google Cloud 建立一組憑證。

### 2-1. 建立 Google Cloud 專案

1. 前往 [Google Cloud Console](https://console.cloud.google.com/)
2. 點左上角專案選單 → **新增專案**
3. 輸入專案名稱（例如 `hotel-system-dev`）→ 建立

### 2-2. 設定 OAuth 同意畫面

1. 左側選單 → **API 和服務** → **OAuth 同意畫面**
2. 使用者類型選 **外部**（測試階段即可）
3. 填寫應用程式名稱、支援電子郵件等必填欄位
4. 在 **測試使用者** 分頁，加入你要用來登入的 Gmail 帳號

### 2-3. 建立 OAuth 用戶端 ID

1. 左側選單 → **API 和服務** → **憑證**
2. 點 **建立憑證** → **OAuth 用戶端 ID**
3. 應用程式類型選 **網頁應用程式**
4. 在 **已授權的重新導向 URI** 加入：

   ```
   http://localhost:8081/callback
   ```

   > 如果你之後用 Docker 啟動（port 8080），也要再加一條：
   >
   > ```
   > http://localhost:8080/callback
   > ```

5. 建立後，記下 **用戶端 ID** 和 **用戶端密鑰**（下一步會用到）

---

## 第三步：建立環境變數檔

專案根目錄有一份範例檔 `.env.example`，請複製成 `.env` 並填入你的 Google 憑證：

```bash
cp .env.example .env
```

用任何文字編輯器打開 `.env`，修改成：

```env
GOOGLE_CLIENT_ID=你的-google-用戶端-id
GOOGLE_CLIENT_SECRET=你的-google-用戶端密鑰
```

> **注意：** `.env` 含有機密資訊，已被 `.gitignore` 排除，請勿上傳到 GitHub。

---

## 第四步：啟動專案

在專案根目錄執行：

```bash
./mvnw spring-boot:run
```

第一次執行會下載相依套件，可能需要幾分鐘，請耐心等候。

看到類似以下訊息，代表啟動成功：

```
Started HotelSystemApplication in X.XXX seconds
```

預設會在 **port 8081** 啟動。

### macOS / Linux 權限問題

如果 `./mvnw` 無法執行，先給予執行權限：

```bash
chmod +x mvnw
```

### Windows 使用者

請改用：

```cmd
mvnw.cmd spring-boot:run
```

---

## 第五步：打開瀏覽器測試

啟動成功後，在瀏覽器開啟：

| 頁面 | 網址 |
|------|------|
| 登入頁 | http://localhost:8081/login |
| 首頁（登入後） | http://localhost:8081/ |
| H2 資料庫控制台 | http://localhost:8081/h2-console |

### 登入流程

1. 打開 http://localhost:8081/login
2. 點 **使用 Google 登入**
3. 選擇你在 OAuth 測試使用者中新增的 Gmail 帳號
4. 登入成功後會導向首頁，可編輯個人資料

### H2 資料庫連線資訊（進階）

如果想查看資料庫內容，到 http://localhost:8081/h2-console 填入：

| 欄位 | 值 |
|------|-----|
| JDBC URL | `jdbc:h2:mem:hoteldb` |
| User Name | `sa` |
| Password | （留空） |

---

## 用 Docker 啟動（可選）

如果你已安裝 Docker，也可以用容器方式啟動：

```bash
# 確認 .env 已設定好 Google 憑證
docker compose up --build
```

啟動後網址為 http://localhost:8080/login（注意 port 是 **8080**，不是 8081）。

停止服務：

```bash
docker compose down
```

---

## 常用指令

```bash
# 啟動開發伺服器（本機 port 8081）
./mvnw spring-boot:run

# 執行測試
./mvnw test

# 打包成 JAR 檔
./mvnw package -DskipTests

# 執行打包好的 JAR
java -jar target/hotel-system-0.0.1-SNAPSHOT.jar
```

---

## 專案結構

```
hotel-system/
├── src/main/java/com/hotel/system/
│   ├── config/          # 安全性與 OAuth2 設定
│   ├── controller/      # 網頁與 API 路由
│   ├── entity/          # 資料庫實體（User 等）
│   ├── repository/      # 資料存取層
│   └── service/         # 商業邏輯
├── src/main/resources/
│   ├── application.yml  # 應用程式設定（port、資料庫等）
│   ├── static/          # CSS、JS 靜態檔案
│   └── templates/       # Thymeleaf 網頁模板
├── docker-compose.yml   # Docker 啟動設定
├── Dockerfile           # Docker 映像檔建置
├── .env.example         # 環境變數範例
└── pom.xml              # Maven 專案設定
```

---

## 常見問題

### Q1：`java: command not found`

代表尚未安裝 Java，或終端機找不到 Java。請安裝 [JDK 21](https://adoptium.net/)，安裝完重新開啟終端機再試。

### Q2：登入時出現 `redirect_uri_mismatch`

Google OAuth 的重新導向 URI 與實際不符。請確認 Google Cloud 憑證中已加入：

- 本機開發：`http://localhost:8081/callback`
- Docker：`http://localhost:8080/callback`

### Q3：點 Google 登入沒反應或出現錯誤

請檢查：

1. `.env` 是否存在且 `GOOGLE_CLIENT_ID`、`GOOGLE_CLIENT_SECRET` 已正確填入
2. 你的 Gmail 是否已加入 OAuth 同意畫面的 **測試使用者**
3. 修改 `.env` 後需**重新啟動**應用程式

### Q4：port 8081 已被占用

可以改用其他 port 啟動：

```bash
PORT=8082 ./mvnw spring-boot:run
```

同時記得在 Google Cloud 的重新導向 URI 加上 `http://localhost:8082/callback`。

### Q5：`./mvnw` 下載很慢

第一次執行會從 Maven 中央倉庫下載套件，視網路速度可能需要數分鐘。若在中國大陸等地區，可考慮設定 Maven 鏡像加速（進階設定）。

### Q6：測試可以過，但本機啟動失敗

先確認 `.env` 已設定。測試環境會使用假的 Google 憑證（見 `src/test/resources/application-test.yml`），但實際啟動需要真實憑證才能登入。

---

## 技術棧

- Java 21
- Spring Boot 3.5
- Spring Security + OAuth2 Client
- Spring Data JPA
- H2 Database（記憶體模式）
- Thymeleaf 模板引擎
- Docker / Docker Compose

---

## 授權

本專案為課程／團隊開發用途，詳細授權條款請參考專案維護者說明。