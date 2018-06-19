
#In Git Bash. Change the current working directory to your local project.

git init
#---------------------------------------------------------------------------------------------------------------------------------------------------------------
# Add the files in your new local repository. This stages them for the first commit.

git add .
							# Adds the files in the local repository and stages them for commit. To unstage a file, use 'git reset HEAD YOUR-FILE'.
#---------------------------------------------------------------------------------------------------------------------------------------------------------------
# Commit the files that you've staged in your local repository.

git commit -m "With 0420 and repeat 0421 working"
							#-Commits the tracked changes. To remove this commit and modify the file, use 'git reset --soft HEAD~1' and commit and add the file again.
#---------------------------------------------------------------------------------------------------------------------------------------------------------------

# In the Command prompt, add the URL for the remote repository where your local repository will be pushed.
					  
git remote add origin https://github.com/qmafake/SbzRestApi.git	# Sets the new remote
git remote -v													# Verifies the new remote URL
#---------------------------------------------------------------------------------------------------------------------------------------------------------------

#Push the changes in your local repository to GitHub.

git push origin master
#---------------------------1------------------------------------------------------------------------------------------------------------------------------------

# MISC
git rm --cached <file>  	# ro remove a tracked file

#---------------------------------------------------------------------------------------------------------------------------------------------------------------

# To clone to a different repository
#git remote add work https://github.com/onebalance/MeterHunter.git
#git push -u work master