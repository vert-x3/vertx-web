# fail for python 3 since sockjs requires python 2.7
import sys

if (sys.version_info > (3, 0)):
  raise RuntimeError('you are running python 3.x')
