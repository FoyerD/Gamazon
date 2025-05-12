import { n, a, c as r } from "./copilot-wUxCZUSx.js";
/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
function c(t) {
  return n({ ...t, state: !0, attribute: !1 });
}
class u extends a {
  constructor() {
    super(...arguments), this.eventBusRemovers = [], this.messageHandlers = {};
  }
  createRenderRoot() {
    return this;
  }
  onEventBus(e, s) {
    this.eventBusRemovers.push(r.on(e, s));
  }
  disconnectedCallback() {
    super.disconnectedCallback(), this.eventBusRemovers.forEach((e) => e());
  }
  onCommand(e, s) {
    this.messageHandlers[e] = s;
  }
  handleMessage(e) {
    return this.messageHandlers[e.command] ? (this.messageHandlers[e.command].call(this, e), !0) : !1;
  }
}
export {
  u as B,
  c as r
};
