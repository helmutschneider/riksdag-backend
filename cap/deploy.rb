# config valid only for current version of Capistrano
lock '3.5.0'

set :application, 'api.riksdagskollen.se'
set :repo_url, 'https://github.com/helmutschneider/riksdag-backend.git'

# Default branch is :master
# ask :branch, `git rev-parse --abbrev-ref HEAD`.chomp

# Default deploy_to directory is /var/www/my_app_name
set :deploy_to, "/var/www/#{fetch(:application)}"

# Default value for :scm is :git
# set :scm, :git

# Default value for :format is :pretty
# set :format, :pretty

# Default value for :log_level is :debug
set :log_level, :debug

# Default value for :pty is false
# set :pty, true

# Default value for :linked_files is []
set :linked_files, fetch(:linked_files, []).push('.env')

# Default value for linked_dirs is []
# set :linked_dirs, fetch(:linked_dirs, []).push('log', 'tmp/pids', 'tmp/cache', 'tmp/sockets', 'vendor/bundle', 'public/system')

# Default value for default_env is {}
# set :default_env, { path: "/opt/ruby/bin:$PATH" }

# Default value for keep_releases is 5
set :keep_releases, 3

set :jar_path, 'target/scala-2.11/app.jar'
set :listen_port, 6000

# Custom deployment actions so we can upload the compiled JAR
# instead of using git as capistrano usually does...
namespace :app do

    task :compile do
        on roles(:all) do
            within release_path do
                execute './sbt', :assembly
            end
        end
    end

    task :stop do
      on roles(:all) do
         within release_path do
             execute './scripts/init.sh', :stop
         end
      end
    end

    task :start do
      on roles(:all) do
        within release_path do
            execute "PORT=#{fetch(:listen_port)}", "ENV_PATH=#{shared_path}/.env", './scripts/init.sh', :start, "#{current_path}/#{fetch(:jar_path)}"
        end
      end
    end

end

after   'deploy:updated',       'app:compile'
before  'deploy:publishing',    'app:stop'
after   'deploy:publishing',    'app:start'
