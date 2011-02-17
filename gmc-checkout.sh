# cleans the workspace and checks out gmc projects in to it

cd ${WORKSPACE}
rm -rf *
git clone /data/private/gmc-src.git
git clone /data/private/gmc-target.git
