// Usage:
//   k6 run load-test/orders-load-test.js
//   k6 run -e BASE_URL=http://localhost:8080 -e VUS=100 -e DURATION=1m load-test/orders-load-test.js
//
// Seed users (data.sql) only start with 10,000 / 5,000 points, which would run out
// after a few orders. setup() charges both with a large point balance first so the
// run measures throughput/latency instead of "ran out of points" failures.

import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const CHARGE_AMOUNT = Number(__ENV.CHARGE_AMOUNT || 100000000);
const VUS = Number(__ENV.VUS || 50);
const RAMP_UP = __ENV.RAMP_UP || '10s';
const HOLD = __ENV.DURATION || '30s';
const RAMP_DOWN = __ENV.RAMP_DOWN || '10s';

const USER_IDS = [1, 2];
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
    order_success_rate: ['rate>0.9'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)'],
};

export function setup() {
  const headers = { 'Content-Type': 'application/json' };

  USER_IDS.forEach((userId) => {
    const res = http.post(
      `${BASE_URL}/points/charge`,
      JSON.stringify({ userId, amount: CHARGE_AMOUNT }),
      { headers }
    );
    check(res, {
      [`user ${userId} point charge succeeded`]: (r) => r.status === 200,
    });
  });
}

export default function () {
  const userId = USER_IDS[Math.floor(Math.random() * USER_IDS.length)];
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
