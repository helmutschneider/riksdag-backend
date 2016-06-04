# Riksdagskollen backend

## Dependencies
- java 1.8+
- ruby & bundler gem (only for deployment)

## Development
Start the dev server listening on localhost:8080:
```shell
./sbt "~;jetty:stop;jetty:start"
```

## Building for production
Compile the app and all its dependencies to `target/scala-2.11/app.jar`:
```shell
./sbt assembly
```

## Deploying
Deploy a new version to the server:
```shell
bundle exec cap production deploy
```

Start/stop the app:
```shell
bundle exec cap production deploy:stop_app
bundle exec cap production deploy:start_app
```
