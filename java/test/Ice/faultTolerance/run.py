#!/usr/bin/env python
# **********************************************************************
#
# Copyright (c) 2001
# MutableRealms, Inc.
# Huntsville, AL, USA
#
# All Rights Reserved
#
# **********************************************************************

import os, sys

for toplevel in [".", "..", "../..", "../../..", "../../../.."]:
    toplevel = os.path.normpath(toplevel)
    if os.path.exists(os.path.join(toplevel, "config", "TestUtil.py")):
        break
else:
    raise "can't find toplevel directory!"

sys.path.append(os.path.join(toplevel, "config"))
import TestUtil

testdir = os.path.join(toplevel,"test", "Ice", "faultTolerance")
classpath = os.path.join(toplevel, "lib") + TestUtil.sep + os.path.join(testdir, "classes") + \
    TestUtil.sep + os.getenv("CLASSPATH", "")
server = "java -classpath \"" + classpath + "\" Server"
client = "java -classpath \"" + classpath + "\" Client"

num = 8
base = 12340

updatedServerOptions = TestUtil.serverOptions.replace("TOPLEVELDIR", toplevel)
updatedClientOptions = TestUtil.clientOptions.replace("TOPLEVELDIR", toplevel)

serverPipes = { }
for i in range(0, num):
    print "starting server #%d..." % (i + 1),
    serverPipes[i] = os.popen(server + updatedServerOptions + " %d" % (base + i))
    TestUtil.getAdapterReady(serverPipes[i])
    print "ok"

ports = ""
for i in range(0, num):
    ports = "%s %d" % (ports, base + i)
print "starting client...",
clientPipe = os.popen(client + updatedClientOptions + " " + ports)
print "ok"

for output in clientPipe.xreadlines():
    print output,

for i in range(0, num):
    serverPipes[i].close()

clientStatus = clientPipe.close()
serverStatus = None
for i in range(0, num):
    serverStatus = serverStatus or serverPipes[i].close()

if clientStatus or serverStatus:
    sys.exit(1)

sys.exit(0)
