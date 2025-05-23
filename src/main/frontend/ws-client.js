import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const waitForUserId = () => {
  return new Promise((resolve) => {
    const check = () => {
      const userId = window.currentUserId || sessionStorage.getItem('currentUserId');
      if (userId) {
        resolve(userId);
      } else {
        setTimeout(check, 50);
      }
    };
    check();
  });
};

const showNotification = (message) => {
  // Create a Vaadin notification directly
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
    if (root.firstElementChild) {
      root.removeChild(root.firstElementChild);
    }
    root.appendChild(content);
  };
  
  document.body.appendChild(notification);
  notification.open();
  
  // Remove the notification element after it's closed
  setTimeout(() => {
    document.body.removeChild(notification);
  }, 10000);
};

let stompClient = null;

const connectWebSocket = (userId) => {
  if (stompClient) {
    try {
      stompClient.deactivate();
    } catch (e) {
      console.error('[WS] Error deactivating previous connection:', e);
    }
  }

  const socket = new SockJS('/ws');
  stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
      userId: userId
    },
    onConnect: () => {
      console.log(`[WS] Connected as ${userId}`);
      stompClient.subscribe(`/user/${userId}/topic/notifications`, (message) => {
        console.log('[WS] Received notification:', message.body);
        showNotification(message.body);
      });
    },
    onStompError: (frame) => {
      console.error('[WS] STOMP error:', frame);
    },
    onWebSocketClose: () => {
      console.log('[WS] Connection closed, attempting to reconnect...');
      setTimeout(() => connectWebSocket(userId), 5000);
    },
    reconnectDelay: 5000
  });

  stompClient.activate();
};

// Listen for userId changes
window.addEventListener('storage', (event) => {
  if (event.key === 'currentUserId') {
    const userId = event.newValue;
    if (userId) {
      connectWebSocket(userId);
    }
  }
});

waitForUserId().then((userId) => {
  // Store userId in sessionStorage for cross-window access
  sessionStorage.setItem('currentUserId', userId);
  connectWebSocket(userId);
});
