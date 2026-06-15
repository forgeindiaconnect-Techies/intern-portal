/* profile.js */
document.addEventListener('DOMContentLoaded', () => {
  const photoInput = document.getElementById('profilePhotoInput');
  const previewImg = document.getElementById('profilePhotoPreview');
  if (photoInput && previewImg) {
    photoInput.addEventListener('change', () => {
      const file = photoInput.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = e => { previewImg.src = e.target.result; };
        reader.readAsDataURL(file);
      }
    });
  }
});
