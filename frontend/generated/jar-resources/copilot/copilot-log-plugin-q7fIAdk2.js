import { s as u, M as g, x as r, t as c } from "./copilot-wUxCZUSx.js";
import { B as f, r as h } from "./base-panel-mzEliO7X.js";
var m = Object.defineProperty, v = Object.getOwnPropertyDescriptor, l = (t, e, s, n) => {
  for (var i = n > 1 ? void 0 : n ? v(e, s) : e, o = t.length - 1, d; o >= 0; o--)
    (d = t[o]) && (i = (n ? d(e, s, i) : d(i)) || i);
  return n && i && m(e, s, i), i;
};
let a = class extends f {
  constructor() {
    super(), this.nextMessageId = 1, this.transitionDuration = 0, this.handleLogEvent = (t) => {
      const e = t.data;
      this.log(e.type, e.message, e.details, e.link);
    }, this.notifications = [], this.unreadErrors = !1, this.messages = [], this.catchErrors();
  }
  connectedCallback() {
    super.connectedCallback(), this.onCommand("log", this.handleLogEvent), this.transitionDuration = parseInt(
      window.getComputedStyle(this).getPropertyValue("--dev-tools-transition-duration"),
      10
    );
  }
  activate() {
    this.unreadErrors = !1, this.updateComplete.then(() => {
      const t = this.renderRoot.querySelector(".message-tray .message:last-child");
      t && t.scrollIntoView();
    });
  }
  format(t) {
    return t.message ? t.message.toString() : t.toString();
  }
  catchErrors() {
    const t = window.Vaadin.ConsoleErrors;
    window.Vaadin.ConsoleErrors = {
      push: (e) => {
        u.attentionRequiredPanelTag = p.tag, this.log(g.ERROR, e.map((s) => this.format(s)).join(" ")), t.push(e);
      }
    };
  }
  render() {
    return r`<div class="message-tray">${this.messages.map((t) => this.renderMessage(t))}</div>`;
  }
  renderMessage(t) {
    return r`
      <div
        class="message ${t.type} ${t.deleted ? "animate-out" : ""} ${t.details || t.link ? "has-details" : ""}">
        <div class="message-content">
          <div class="message-heading">${t.message}</div>
          <div class="message-details" ?hidden="${!t.details && !t.link}">
            ${t.details ? r`<p>${t.details}</p>` : ""}
            ${t.link ? r`<a class="ahreflike" href="${t.link}" target="_blank">Learn more</a>` : ""}
          </div>
          ${t.persistentId ? r`<div
                class="persist ${t.dontShowAgain ? "on" : "off"}"
                @click=${() => this.toggleDontShowAgain(t.id)}>
                Don’t show again
              </div>` : ""}
        </div>
      </div>
    `;
  }
  toggleDontShowAgain(t) {
    const e = this.findNotificationIndex(t);
    if (e !== -1 && !this.notifications[e].deleted) {
      const s = this.notifications[e];
      s.dontShowAgain = !s.dontShowAgain, this.requestUpdate();
    }
  }
  findNotificationIndex(t) {
    let e = -1;
    return this.notifications.some((s, n) => s.id === t ? (e = n, !0) : !1), e;
  }
  log(t, e, s, n) {
    const i = this.nextMessageId;
    for (this.nextMessageId += 1, this.messages.push({
      id: i,
      type: t,
      message: e,
      details: s,
      link: n,
      dontShowAgain: !1,
      deleted: !1
    }); this.messages.length > a.MAX_LOG_ROWS; )
      this.messages.shift();
    this.requestUpdate(), this.updateComplete.then(() => {
      const o = this.renderRoot.querySelector(".message-tray .message:last-child");
      o ? (setTimeout(() => o.scrollIntoView({ behavior: "smooth" }), this.transitionDuration), this.unreadErrors = !1) : t === g.ERROR && (this.unreadErrors = !0);
    });
  }
};
a.MAX_LOG_ROWS = 1e3;
l([
  h()
], a.prototype, "notifications", 2);
l([
  h()
], a.prototype, "unreadErrors", 2);
l([
  h()
], a.prototype, "messages", 2);
a = l([
  c("copilot-log-panel")
], a);
const p = {
  header: "Log",
  expanded: !0,
  draggable: !0,
  panelOrder: 0,
  panel: "bottom",
  floating: !1,
  tag: "copilot-log-panel"
}, w = {
  init(t) {
    t.addPanel(p);
  }
};
window.Vaadin.copilot.plugins.push(w);
export {
  a as CopilotLogPanel
};
