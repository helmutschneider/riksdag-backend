# config valid only for current version of Capistrano
lock '3.4.0'

set :application, 'Riksdagskoll-API'
set :repo_url, 'https://github.com/helmutschneider/riksdag-backend.git'

# Default branch is :master
# ask :branch, `git rev-parse --abbrev-ref HEAD`.chomp

# Default deploy_to directory is /var/www/my_app_name
set :deploy_to, "/home/deployer/#{fetch(:application)}"

# Default value for :scm is :git
# set :scm, :git

# Default value for :format is :pretty
# set :format, :pretty

# Default value for :log_level is :debug
# set :log_level, :debug

# Default value for :pty is false
# set :pty, true

# Default value for :linked_files is []
set :linked_files, fetch(:linked_files, []).push('conf/environment.conf')

# Default value for linked_dirs is []
# set :linked_dirs, fetch(:linked_dirs, []).push('log', 'tmp/pids', 'tmp/cache', 'tmp/sockets', 'vendor/bundle', 'public/system')

# Default value for default_env is {}
# set :default_env, { path: "/opt/ruby/bin:$PATH" }

# Default value for keep_releases is 5
set :keep_releases, 3

# Custom deployment actions so we can upload the compiled JAR
# instead of using git as capistrano usually does...
namespace :deploy do

    task :clean_compile_stage do
        on roles(:all) do
            within release_path do
                execute './activator', 'clean', 'compile', 'stage'
            end
        end
    end

    task :stop_app do
      on roles(:all) do
        within current_path do
          if test " [ -f #{current_path}/target/universal/stage/RUNNING_PID ] "
              execute :kill, '$(cat target/universal/stage/RUNNING_PID)'
              sleep 3
          end
        end
      end
    end

    task :start_app do
      on roles(:all) do
        execute "#{release_path}/target/universal/stage/bin/play-scala > /dev/null 2>&1 &"
      end
    end

end

after 'deploy:updated', 'deploy:clean_compile_stage'
before 'deploy:publishing', 'deploy:stop_app'
after 'deploy:publishing', 'deploy:start_app'
