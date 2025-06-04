import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

let stompClient = null;

const showNotification = (message, type = 'info') => {
  const notification = document.createElement('vaadin-notification');
  notification.position = 'top-stretch';
  notification.duration = 10000;

  const content = document.createElement('div');
  content.style.padding = '1em';
  content.style.background = type === 'error' ? '#f44336' : '#ff9800';
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

const disableInteractiveElements = () => {
  // Disable all interactive elements except view cart
  const interactiveSelectors = [
    'button:not(.view-only):not([data-view-cart])',
    'input:not(.view-only)',
    'select:not(.view-only)',
    'a:not(.view-only)',
    'vaadin-button:not(.view-only):not([data-view-cart])',
    'vaadin-text-field:not(.view-only)',
    'vaadin-number-field:not(.view-only)',
    'vaadin-combo-box:not(.view-only)',
    'vaadin-date-picker:not(.view-only)',
    'vaadin-time-picker:not(.view-only)',
    'vaadin-grid:not(.view-only)',
    'vaadin-select:not(.view-only)',
    'vaadin-checkbox:not(.view-only)',
    'vaadin-radio-button:not(.view-only)',
    '[role="button"]:not(.view-only):not([data-view-cart])',
    '[tabindex]:not(.view-only):not([data-view-cart])',
    '[data-trading-button]',  // Always disable trading button for banned users
    '[data-checkout-button]'  // Always disable checkout button for banned users
  ].join(',');

  document.querySelectorAll(interactiveSelectors).forEach(el => {
    // Disable the element
    el.disabled = true;
    el.setAttribute('disabled', 'true');
    el.style.pointerEvents = 'none';
    el.style.opacity = '0.5';
    el.style.cursor = 'not-allowed';

    // For Vaadin components, ensure they're properly disabled
    if (el.tagName.toLowerCase().startsWith('vaadin-')) {
      el.setAttribute('aria-disabled', 'true');
      if (typeof el.disable === 'function') {
        el.disable();
      }
    }
  });

  // Disable all grid actions except view cart
  document.querySelectorAll('vaadin-grid').forEach(grid => {
    const actionColumns = Array.from(grid.querySelectorAll('vaadin-grid-column'))
      .filter(col => col.header && (
        col.header.toLowerCase().includes('action') || 
        col.header.toLowerCase().includes('edit') ||
        col.header.toLowerCase().includes('delete') ||
        (col.header.toLowerCase().includes('cart') && !col.header.toLowerCase().includes('view'))
      ));
    
    actionColumns.forEach(col => {
      if (col.parentNode) {
        col.parentNode.removeChild(col);
      }
    });
  });
};

const enableInteractiveElements = () => {
  // Re-enable all interactive elements
  const interactiveSelectors = [
    'button:not(.view-only)',
    'input:not(.view-only)',
    'select:not(.view-only)',
    'a:not(.view-only)',
    'vaadin-button:not(.view-only)',
    'vaadin-text-field:not(.view-only)',
    'vaadin-number-field:not(.view-only)',
    'vaadin-combo-box:not(.view-only)',
    'vaadin-date-picker:not(.view-only)',
    'vaadin-time-picker:not(.view-only)',
    'vaadin-grid:not(.view-only)',
    'vaadin-select:not(.view-only)',
    'vaadin-checkbox:not(.view-only)',
    'vaadin-radio-button:not(.view-only)',
    '[role="button"]:not(.view-only)',
    '[tabindex]:not(.view-only)',
    '[data-trading-button]'
  ].join(',');

  document.querySelectorAll(interactiveSelectors).forEach(el => {
    // Enable the element
    el.disabled = false;
    el.removeAttribute('disabled');
    el.style.pointerEvents = '';
    el.style.opacity = '';
    el.style.cursor = '';

    // For Vaadin components, ensure they're properly enabled
    if (el.tagName.toLowerCase().startsWith('vaadin-')) {
      el.removeAttribute('aria-disabled');
      if (typeof el.enable === 'function') {
        el.enable();
      }
    }
  });

  // Restore cart button style
  const cartBtn = document.querySelector('[data-view-cart]');
  if (cartBtn) {
    cartBtn.style.backgroundColor = '#38a169';
    cartBtn.style.color = 'white';
    cartBtn.style.border = '';
  }

  // Restore trading button style
  const tradingBtn = document.querySelector('[data-trading-button]');
  if (tradingBtn) {
    tradingBtn.style.backgroundColor = '#4299e1';
    tradingBtn.style.color = 'white';
    tradingBtn.style.cursor = '';
    tradingBtn.style.opacity = '';
  }

  // Delay page refresh to ensure notification is visible
  setTimeout(() => {
    window.location.reload();
  }, 3000); // 3 second delay before refresh
};

const connectWebSocket = (userId) => {
  if (!userId) return;

  if (stompClient) {
    try { stompClient.deactivate(); } catch (e) {}
  }

  const socket = new SockJS(`/ws?userId=${userId}`);

  stompClient = new Client({
    webSocketFactory: () => socket,
    connectHeaders: { userId },
    debug: (msg) => console.log('[STOMP DEBUG]', msg),
    onConnect: () => {
      const destination = '/user/topic/notifications';
      stompClient.subscribe(destination, (message) => {
        console.log('[WS] 🔔 Message received:', message.body);
        try {
          const payload = JSON.parse(message.body);

          if (payload.type === 'USER_BANNED') {
            disableInteractiveElements();
            showNotification(payload.message || 'You have been banned.', 'error');
          } else if (payload.type === 'USER_UNBANNED') {
            showNotification(payload.message || 'Your ban has been lifted. The page will refresh in 3 seconds...', 'success');
            enableInteractiveElements();
          } else {
            showNotification(payload.message || message.body);
          }
        } catch (err) {
          console.warn('[WS] Could not parse JSON, showing raw message');
          showNotification(message.body);
        }
      });
    },
    reconnectDelay: 5000
  });

  console.log('[WS] Activating STOMP client...');
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
