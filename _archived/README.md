# Archived

Code and assets that were part of earlier project scopes (the trust-aware
retail-robot prototype) and have been removed from the active build per
sprint B11.1.

These directories are intentionally outside the build path:

- `robotics/` — ROS 2 workspace, Isaac Sim worlds and scenarios, robotics docs.
  Not referenced by the active Aisleon services and not started by
  `docker compose up`.
- `spring-boot-robotics-stub/` — empty `package-info.java` files that used to
  scaffold a `com.aisleon.robotics` Java package. Moved out of
  `backend/spring-boot/src/main/java/` so Maven no longer compiles them.

Do not re-enable from here without a fresh sprint plan — the current Aisleon
product (UK shopping assistant + social) does not need them.
