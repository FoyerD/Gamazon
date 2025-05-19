import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const waitForUserId = () => {
  return new Promise((resolve) => {
    const check = () => {
      if (window.currentUserId) {
        resolve(window.currentUserId);
      } else {
        setTimeout(check, 50);
      }
    };
    check();
  });
};

waitForUserId().then((userId) => {
  const socket = new SockJS('/ws');
  const stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: {
      userId: userId
    },
    onConnect: () => {
      console.log(`[WS] Connected as ${userId}`);
      stompClient.subscribe(`/user/${userId}/topic/notifications`, (message) => {
        alert(`🔔 ${message.body}`);
      });
    },
    onStompError: (frame) => {
      console.error('[WS] STOMP error:', frame);
    },
    reconnectDelay: 5000
  });

  stompClient.activate();
});
