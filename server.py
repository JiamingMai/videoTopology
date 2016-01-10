#/usr/bin/python
import os
import sys
import numpy as np
import caffe
import socket
import time
import struct

# Load the trained CNN model
print 'Original Path: ', os.getcwd()
os.chdir('/usr/local/digits-2.0/caffe/examples')
print 'Current Path: ', os.getcwd()
caffe_root = '../'
caffe.set_mode_gpu()
net = caffe.Net(caffe_root + 'models/vgg16/VGG_ILSVRC_16_layers_deploy.prototxt', caffe_root + 'models/vgg16/VGG_ILSVRC_16_layers.caffemodel', caffe.TEST)

transformer = caffe.io.Transformer({'data': net.blobs['data'].data.shape})
transformer.set_transpose('data', (2,0,1))
transformer.set_mean('data',np.array([103.939, 116.779, 123.68]))
#transformer.set_raw_scale('data', 255)
transformer.set_channel_swap('data', (2,1,0))
net.blobs['data'].reshape(1,3,224,224)

host="localhost"
port=8881
s=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
s.bind((host,port))
s.listen(5)
hei = 0
wid = 0
while 1:
    print "-------------------"    

    # Receive the data from Java program
    start = time.time()

    ## read the height of the image
    data = ''
    sock,addr=s.accept()
    while len(data) < 4:
        data += sock.recv(4 - len(data))
    hei = struct.unpack('!i', data)[0]
    print 'height: ', hei

    ## read the width of the image
    data = ''
    while len(data) < 4:
        data += sock.recv(4 - len(data))
    wid = struct.unpack('!i', data)[0]
    print 'width: ', wid

    ## read the size of the image
    data = ''
    while len(data) < 4:
        data += sock.recv(4 - len(data))
    size = struct.unpack('!i', data)[0]
    print 'size: ', size, ' ', type(size)

    ## read the image data
    buf = ''
    data = ''
    while size > 0:
        buf = sock.recv(1024)
        size -= len(buf)
        data += buf
    print 'The length of data is ', len(data)
    end = time.time()
    print "Receipt time: %f ms" % ((end - start)*1000)
    
    # Arrange the received data for CNN model
    start = time.time()
    img = np.frombuffer(data, dtype='b')
    img = img.reshape((hei,wid,3))
    img = img & 0xff
    img = img.astype('float32')
    end = time.time()
    print "Arragement time: %f ms" % ((end - start)*1000)

    # Extract the CNN features of the data
    start = time.time()
    net.blobs['data'].data[...] = transformer.preprocess('data', img)    
    out = net.forward()
    feat = net.blobs['fc7'].data[0]
    fl = list(feat)
    #print fl    
    end = time.time()
    print "Extraction time: %f ms" % ((end - start)*1000)
    
    # Send back the features to Java Program
    start = time.time()
    fl = str(fl)
    fl = fl[1:-1]
    sock.send(str(fl)+'\n')     
    print 'testing...'
    end = time.time()
    print "Transmit time: %f ms" % ((end - start)*1000)

    print "-------------------"
    
s.close()
