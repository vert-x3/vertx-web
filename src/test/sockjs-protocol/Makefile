

#### General

all: pycco_deps test_deps build

build: pycco_deps
	./venv/bin/pycco sockjs-protocol*.py

clean:
	rm -rf venv *.pyc


#### Dependencies

venv:
	virtualenv venv
	-rm distribute-*.tar.gz || true

pycco_deps: venv/.pycco_deps
venv/.pycco_deps: venv
	./venv/bin/pip install pycco
	touch venv/.pycco_deps

test_deps: venv/.test_deps
venv/.test_deps: venv
	./venv/bin/pip install unittest2
	./venv/bin/pip install websocket-client==0.4.1
# Main source crashes https://github.com/Lawouach/WebSocket-for-Python/issues/16
	./venv/bin/pip install git+git://github.com/majek/WebSocket-for-Python.git
	touch venv/.test_deps


#### Development

serve: pycco_deps
	@while [ 1 ]; do			\
		make build;			\
		sleep 0.1;			\
		inotifywait -r -q -e modify .;	\
	done


#### Deployment

upload: build
	@node -v > /dev/null
	[ -e ../sockjs-protocol-gh-pages ] || 				\
		git clone `git remote -v|tr "[:space:]" "\t"|cut -f 2`	\
			--branch gh-pages ../sockjs-protocol-gh-pages
	(cd ../sockjs-protocol-gh-pages; git pull;)
	cp docs/* ../sockjs-protocol-gh-pages
	(cd ../sockjs-protocol-gh-pages; git add pycco.css sockjs*html; git commit sockjs*html -m "Content regenerated";)
	(cd ../sockjs-protocol-gh-pages; node generate_index.js > index.html;)
	(cd ../sockjs-protocol-gh-pages; git add index.html; git commit index.html -m "Index regenerated";)
	@echo ' [*] Now run:'
	@echo '(cd ../sockjs-protocol-gh-pages; git push;)'
