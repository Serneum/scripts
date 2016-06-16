SETUP_FILE=/home/vagrant/setup.sh

# custom.sh is run as the root user. Create a shell script that we can run as
# the vagrant user
cat <<EOF > $SETUP_FILE
# Clone settings repo
git clone https://github.com/Serneum/dotfiles.git .dotfiles
./.dotfiles/install

# Set up Vim, Vundle, and install plugins from the .vimrc file
if [ ! -d ~/.vim ]; then
  mkdir ~/.vim
  git clone https://github.com/VundleVim/Vundle.vim.git ~/.vim/bundle/Vundle.vim
  vundle
fi
brew install ag autojump cheat
EOF

# Give permissions for the script to the vagrant user
chown vagrant $SETUP_FILE
chmod +x $SETUP_FILE

# Run the script
su - vagrant -c "sh $SETUP_FILE"
rm $SETUP_FILE
