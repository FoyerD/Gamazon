import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

let stompClient = null;

const showNotification = (message) => {
  const notification = document.createElement('vaadin-notification');
  notification.position = 'top-stretch';
  notification.duration = 10000;
  const content = document.createElement('div');
  content.style.padding = '1em';
  content.style.background = '#ff9800';
  content.style.color = 'white';
  content.style.textAlign = 'center';
  content.style.fontSize = 'var(--lumo-font-size-m)';
  content.textContent = message;
  notification.renderer = (root) => {
    root.innerHTML = '';
    root.appendChild(content);
  };
  document.body.appendChild(notification);
  notification.open();
  setTimeout(() => document.body.removeChild(notification), 10000);
};

const connectWebSocket = (userId) => {
  if (!userId) return;

  if (stompClient) {
    try { stompClient.deactivate(); } catch (e) {}
  }

  const socket = new SockJS('/ws');
  stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: { userId },
    debug: (msg) => console.log('[STOMP DEBUG]', msg),
    onConnect: () => {
      const destination = '/user/topic/notifications';
      stompClient.subscribe(destination, (message) => {
        console.log('[WS] 🔔 Notification received:', message.body);
        showNotification(message.body);
      });
    },
    reconnectDelay: 5000
  });

  stompClient.activate();
  console.log('[WS] Sent CONNECT with userId =', userId);
  window.stompClient = stompClient;
};

window.connectWebSocket = connectWebSocket;

const waitForUserIdAndConnect = () => {
  if (window.currentUserId) {
    sessionStorage.setItem('currentUserId', window.currentUserId);
    connectWebSocket(window.currentUserId);
  } else {
    setTimeout(waitForUserIdAndConnect, 100);
  }
};

waitForUserIdAndConnect();

window.addEventListener('storage', (e) => {
  if (e.key === 'currentUserId' && e.newValue)
    connectWebSocket(e.newValue);
});
