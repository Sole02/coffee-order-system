// Pure-throughput variant: spreads orders across a wide user pool (ids 3~102, each
// pre-seeded with a huge point balance) instead of the 2 default seed users, so
// results aren't skewed by optimistic-lock contention on a couple of rows or by
// running out of points.
//
// Requires the app to be started with the "loadtest" profile so data-loadtest.sql
// is loaded instead of the default data.sql:
//   ./gradlew bootRun --args='--spring.profiles.active=loadtest'
//
// Usage:
//   k6 run -e BASE_URL=http://192.168.128.1:8080 load-test/orders-load-test-pool.js
//   k6 run -e VUS=100 -e DURATION=1m load-test/orders-load-test-pool.js

import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const VUS = Number(__ENV.VUS || 50);
const RAMP_UP = __ENV.RAMP_UP || '10s';
const HOLD = __ENV.DURATION || '30s';
const RAMP_DOWN = __ENV.RAMP_DOWN || '10s';

const USER_ID_MIN = Number(__ENV.USER_ID_MIN || 3);
const USER_ID_MAX = Number(__ENV.USER_ID_MAX || 102);
const MENU_IDS = [1, 2, 3];

const orderSuccess = new Counter('order_success_count');
const orderFailed = new Counter('order_failed_count');
const orderConflict409 = new Counter('order_conflict_409_count');
const insufficientPoint400 = new Counter('order_insufficient_point_400_count');
const orderSuccessRate = new Rate('order_success_rate');

export const options = {
  scenarios: {
    orders_load: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: RAMP_UP, target: VUS },
        { duration: HOLD, target: VUS },
        { duration: RAMP_DOWN, target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],
    order_success_rate: ['rate>0.99'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)'],
};

function randomUserId() {
  return USER_ID_MIN + Math.floor(Math.random() * (USER_ID_MAX - USER_ID_MIN + 1));
}

export default function () {
  const userId = randomUserId();
  const menuId = MENU_IDS[Math.floor(Math.random() * MENU_IDS.length)];

  const res = http.post(
    `${BASE_URL}/orders`,
    JSON.stringify({ userId, menuId }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  const ok = check(res, {
    'status is 200': (r) => r.status === 200,
  });

  orderSuccessRate.add(ok);

  if (ok) {
    orderSuccess.add(1);
  } else {
    orderFailed.add(1);
    if (res.status === 409) orderConflict409.add(1);
    if (res.status === 400) insufficientPoint400.add(1);
  }
}
