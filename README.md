# GracefulStop

Warn players before the server stops or restarts. A countdown runs in chat, in titles and in the action bar, everyone gets a clear message instead of a bare "Server closed", and then the server stops cleanly.

[![Build](https://builds.fadehost.net/FadeHost/GracefulStop/badge.svg)](https://builds.fadehost.net/FadeHost/GracefulStop/)

**[Download the latest build](https://builds.fadehost.net/FadeHost/GracefulStop/latest/GracefulStop.jar)** (always the newest jar) · [every build, with logs](https://builds.fadehost.net/FadeHost/GracefulStop/)

Works on Paper, Purpur, Spigot and Bukkit 1.13 and newer, Java 17 or newer. No dependencies, no config needed to start.

## Commands

| Command | What it does |
| --- | --- |
| `/gstop <seconds> [reason]` | Counts down, then stops the server. `/gstop 300 Nightly backup` |
| `/grestart <seconds> [reason]` | Counts down, then restarts (see below). |
| `/gstop now [reason]` | Skips the countdown: tells everyone, kicks them with the message, stops. |
| `/gstop cancel` | Cancels the running countdown and says so. |
| `/gstop status` | How long is left and why. |
| `/gstop reload` | Re-reads `config.yml`. |

Aliases: `/gracefulstop`, `/stopin`, `/gracefulrestart`, `/restartin`. Permission: `gracefulstop.use` (operators by default). The console can run everything, so a scheduled task like `gstop 120 Daily restart` works on any panel that sends console commands.

## What players see

- A chat line at each mark (by default 30, 20, 15, 10, 5, 3, 2 and 1 minutes, then 30, 15, 10 and every second from 5).
- A title with the remaining time below 60 seconds.
- The action bar counting every second below 10 seconds.
- A note block sound at each announcement.
- A kick message that says why the server is going down, instead of "Server closed".

Every text, the marks and the thresholds are in `config.yml`. Colours use `&` codes; placeholders are `{time}` ("5 minutes", "1 minute 30 seconds"), `{seconds}`, `{reason}`, `{verb}` (stop / restart), `{verbs}` and `{verbing}`.

## Restarting

`/grestart` uses the server's own restart: Paper and Spigot run the `restart-script` from `spigot.yml`. Without a restart script the server shuts down instead, and your host's panel or a scheduled start brings it back. If your host offers a console command that restarts the server, put it in `restart-command`.

## Builds

Every push to `main` is compiled by [FadeHost Builds](https://fadehost.com/builds/), the free build service for public plugin and mod repositories on GitHub. The result is a page with every build, its log and its jar, and one link that always serves the newest one:

https://builds.fadehost.net/FadeHost/GracefulStop/

FadeHost customers can install any of those builds into a server from the panel. We run this repository through the same service our customers use.

## Building it yourself

    mvn -B package

The jar lands in `target/GracefulStop.jar`.

## License

MIT, see [LICENSE](LICENSE).
