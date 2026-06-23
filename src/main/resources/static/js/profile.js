document.addEventListener('DOMContentLoaded', function () {
    var modal = document.getElementById('profile-modal');
    var trigger = document.getElementById('profile-trigger');
    var closeBtn = document.getElementById('profile-close');
    var cancelBtn = document.getElementById('profile-cancel');
    var avatarInput = document.getElementById('avatarUrl');
    var nameInput = document.getElementById('name');
    var previewContainer = document.querySelector('.profile-preview');

    function openModal() {
        modal.hidden = false;
        document.body.classList.add('modal-open');
    }

    function closeModal() {
        modal.hidden = true;
        document.body.classList.remove('modal-open');
    }

    function updatePreview() {
        var url = avatarInput.value.trim();
        var name = nameInput.value.trim();
        var initial = name ? name.charAt(0).toUpperCase() : 'G';

        if (url) {
            previewContainer.innerHTML =
                '<img id="avatar-preview" class="profile-avatar" alt="大頭貼預覽" src="' + url + '">';
        } else {
            previewContainer.innerHTML =
                '<span id="avatar-preview-fallback" class="profile-avatar profile-avatar-fallback">' +
                initial +
                '</span>';
        }
    }

    trigger.addEventListener('click', openModal);
    closeBtn.addEventListener('click', closeModal);
    cancelBtn.addEventListener('click', closeModal);

    modal.addEventListener('click', function (event) {
        if (event.target === modal) {
            closeModal();
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape' && !modal.hidden) {
            closeModal();
        }
    });

    avatarInput.addEventListener('input', updatePreview);
    nameInput.addEventListener('input', updatePreview);
});