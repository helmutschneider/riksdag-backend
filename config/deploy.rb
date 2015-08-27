# config valid only for current version of Capistrano
lock '3.4.0'

set :application, 'Riksdagskoll-API'
set :repo_url, 'git@example.com:me/my_repo.git'

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

set :zip_name, 'play-scala-1.0-SNAPSHOT'

# Default value for :linked_files is []
set :linked_files, fetch(:linked_files, []).push("#{fetch(:zip_name)}/conf/environment.conf")

# Default value for linked_dirs is []
# set :linked_dirs, fetch(:linked_dirs, []).push('log', 'tmp/pids', 'tmp/cache', 'tmp/sockets', 'vendor/bundle', 'public/system')

# Default value for default_env is {}
# set :default_env, { path: "/opt/ruby/bin:$PATH" }

# Default value for keep_releases is 5
set :keep_releases, 3

Rake::Task["deploy:updating"].clear_actions
Rake::Task["deploy:check"].clear_actions
Rake::Task["deploy:set_current_revision"].clear_actions

# Custom deployment actions so we can upload the compiled JAR
# instead of using git as capistrano usually does...
namespace :deploy do

    task :updating => :new_release_path do
      on release_roles(:all) do
        execute :mkdir, '-p', release_path
      end
#     invoke "#{scm}:create_release"
      invoke "deploy:set_current_revision"
      invoke 'deploy:upload_app'
      invoke 'deploy:symlink:shared'
    end

    task :check do
#     invoke "#{scm}:check"
      invoke 'deploy:check:directories'
      invoke 'deploy:check:linked_dirs'
      invoke 'deploy:check:make_linked_dirs'
      invoke 'deploy:check:linked_files'
    end

    task :set_current_revision do
       set :current_revision, "SNAPSHOT"
#      invoke "#{scm}:set_current_revision"
#      on release_roles(:all) do
#        within release_path do
#          execute :echo, "\"#{fetch(:current_revision)}\" >> REVISION"
#        end
#      end
    end

    task :build_dist do
      sh './activator dist'
    end

    task :upload_app do
      on roles(:all) do |host|
        upload! "./target/universal/#{fetch(:zip_name)}.zip", release_path

         within release_path do
           execute 'unzip', "#{fetch(:zip_name)}.zip"
         end

      end
    end

    task :stop_app do
      on roles(:all) do
        within current_path do
          execute :kill, '-9', "$(cat #{fetch(:zip_name)}/RUNNING_PID)"
        end
      end
    end

    task :start_app do
      on roles(:all) do
        execute "#{release_path}/#{fetch(:zip_name)}/bin/play-scala > /dev/null 2>&1 &"
        within release_path do

        end
      end
    end

end

after 'deploy:started', 'deploy:build_dist'
before 'deploy:publishing', 'deploy:stop_app'
after 'deploy:publishing', 'deploy:start_app'
