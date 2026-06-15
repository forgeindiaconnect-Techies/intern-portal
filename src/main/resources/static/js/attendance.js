/* attendance.js */
document.addEventListener('DOMContentLoaded', () => {
  const clockEl = document.getElementById('liveClock');
  if (clockEl) {
    const tick = () => { clockEl.textContent = new Date().toLocaleTimeString(); };
    tick();
    setInterval(tick, 1000);
  }

  const video = document.getElementById('liveCamera');
  const canvas = document.getElementById('captureCanvas');
  const preview = document.getElementById('capturedPreview');
  const photoData = document.getElementById('photoData');
  const startBtn = document.getElementById('startCameraBtn');
  const captureBtn = document.getElementById('capturePhotoBtn');
  const submitBtn = document.getElementById('checkInSubmit');
  const message = document.getElementById('cameraMessage');
  const form = document.getElementById('liveAttendanceForm');

  let cameraStream = null;

  async function startCamera() {
    if (!video || !navigator.mediaDevices?.getUserMedia) {
      if (message) message.textContent = 'Camera is not available in this browser.';
      return;
    }

    try {
      cameraStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: { ideal: 960 }, height: { ideal: 720 } },
        audio: false
      });
      video.srcObject = cameraStream;
      video.style.display = 'block';
      if (preview) preview.style.display = 'none';
      if (captureBtn) captureBtn.disabled = false;
      if (message) message.textContent = 'Camera ready. Capture your live photo.';
    } catch (error) {
      if (message) message.textContent = 'Camera permission is required to mark attendance.';
    }
  }

  function capturePhoto() {
    if (!video || !canvas || !photoData || video.videoWidth === 0) return;

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

    const imageData = canvas.toDataURL('image/jpeg', 0.88);
    photoData.value = imageData;
    if (preview) {
      preview.src = imageData;
      preview.style.display = 'block';
    }
    video.style.display = 'none';
    if (submitBtn) submitBtn.disabled = false;
    if (message) message.textContent = 'Photo captured. You can check in now.';

    if (cameraStream) {
      cameraStream.getTracks().forEach(track => track.stop());
      cameraStream = null;
    }
  }

  if (startBtn) startBtn.addEventListener('click', startCamera);
  if (captureBtn) captureBtn.addEventListener('click', capturePhoto);
  if (form) {
    form.addEventListener('submit', event => {
      if (!photoData?.value) {
        event.preventDefault();
        if (message) message.textContent = 'Please capture your live photo first.';
      }
    });
  }
});
