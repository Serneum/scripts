SETUP_FILE=/home/vagrant/setup.sh

# custom.sh is run as the root user. Create a shell script that we can run as
# the vagrant user
cat <<EOF > $SETUP_FILE
# Clone settings repo
git clone --recursive https://github.com/Serneum/dotfiles.git .dotfiles
./.dotfiles/install

# Prevent the issue where Vim complains about input/output and causes the terminal to need to be closed
echo | echo | vim +PluginInstall +qall &>/dev/null
EOF

# Give permissions for the script to the vagrant user
chown vagrant $SETUP_FILE
chmod +x $SETUP_FILE

# Run the script
su - vagrant -c "sh $SETUP_FILE"
rm $SETUP_FILE
