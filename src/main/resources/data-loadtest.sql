INSERT INTO users (name, point, version) VALUES ('재석', 10000, 0);
INSERT INTO users (name, point, version) VALUES ('테스트유저2', 5000, 0);

-- load-test pool: ids 3~102, large point balance so contention/exhaustion don't skew throughput results
INSERT INTO users (name, point, version)
SELECT CONCAT('loadtest-user-', x), 1000000000, 0
FROM SYSTEM_RANGE(1, 100);

INSERT INTO menus (name, price) VALUES ('아메리카노', 4000);
INSERT INTO menus (name, price) VALUES ('카페라떼', 4500);
INSERT INTO menus (name, price) VALUES ('바닐라라떼', 5000);
