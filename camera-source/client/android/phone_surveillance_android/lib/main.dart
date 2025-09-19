import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:camera/camera.dart';
import 'package:image/image.dart' as img;
import 'package:wakelock_plus/wakelock_plus.dart';
import 'package:permission_handler/permission_handler.dart';
import 'dart:io';
import 'dart:typed_data';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Phone surveillance system',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.accents.first,
          brightness: Brightness.light,
        ),
      ),

      darkTheme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.accents.first,
          brightness: Brightness.dark,
        ),
      ),
      home: const MyHomePage(title: 'Phone surveillance'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  @override
  Widget build(BuildContext context) {
    // This method is rerun every time setState is called
    return SafeArea(
      child: Scaffold(
        appBar: AppBar(
          backgroundColor: Theme.of(context).colorScheme.inversePrimary,
          title: Text(widget.title),
          shadowColor: Colors.black,
          elevation: 5,

          centerTitle: true,

          leading: Padding(
            padding: const EdgeInsets.all(10),
            child: Image.asset('assets/favicon.png', width: 50, height: 50),
          ),
        ),

        body: SingleChildScrollView(
          // Center is a layout widget. It takes a single child and positions it
          // in the middle of the parent.
          child: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            children: <Widget>[
              Padding(padding: EdgeInsets.all(10)),

              CameraParameters(),
            ],
          ),
        ),
      ),
    );
  }
}

class CameraParameters extends StatefulWidget {
  const CameraParameters({super.key});

  @override
  State<CameraParameters> createState() => _CameraParametersState();
}

class _CameraParametersState extends State<CameraParameters> {
  final _formKey = GlobalKey<FormState>();
  final _cameraKey = GlobalKey<_CameraListState>();

  final TextEditingController _cameraName = TextEditingController();
  final TextEditingController _framerate = TextEditingController();
  final TextEditingController _framesPerInterval = TextEditingController();
  final TextEditingController _serverIp = TextEditingController();
  final TextEditingController _serverPort = TextEditingController();

  bool _isStreaming = false;

  @override
  void dispose() {
    _cameraName.dispose();
    _framerate.dispose();
    _framesPerInterval.dispose();
    _serverIp.dispose();
    _serverPort.dispose();

    _controller.dispose().catchError((error) {
      print('Error disposing camera: $error');
    });

    super.dispose();
  }

  late Socket _socket;
  late CameraController _controller;

  Future<void> _initCamera(CameraDescription camera) async {
    try {
      _controller = CameraController(
        camera,
        ResolutionPreset.medium,
        enableAudio: false, // Disable audio if not needed
      );

      await _controller.initialize().catchError((error) {
        if (error is CameraException) {
          print('Camera initialization error: $error');
          throw error;
        }
      });

      if (!_controller.value.isInitialized) {
        throw CameraException(
          'Initialization failed',
          'Camera not initialized',
        );
      }
    } catch (e) {
      print('Camera initialization failed: $e');
      rethrow;
    }
  }

  Future<void> _initCameraWithRetry(
    CameraDescription camera, {
    int retries = 3,
  }) async {
    for (int i = 0; i < retries; i++) {
      try {
        await _initCamera(camera);
        return; // Success
      } catch (e) {
        if (i == retries - 1) rethrow; // Last retry failed
        await Future.delayed(Duration(milliseconds: 500 * (i + 1)));
      }
    }
  }

  Future<void> _connect(String ip, int port) async {
    try {
      _socket = await Socket.connect(ip, port);
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Could not connect to the specified server.'),
        ),
      );
    }
  }

  Future<void> _sendName(Socket socket, String name) async {
    final nameBytes = utf8.encode(name);
    final lengthBuffer = ByteData(4)..setInt32(0, nameBytes.length, Endian.big);
    socket.add(lengthBuffer.buffer.asUint8List());
    socket.add(nameBytes);
    await socket.flush();
  }

  Future<void> _sendFramesPerInterval(Socket socket, int frames) async {
    final buf = ByteData(4)..setInt32(0, frames, Endian.big);
    socket.add(buf.buffer.asUint8List());
    await socket.flush();
  }

  Future<void> _sendFrame(Socket socket, List<int> jpegBytes) async {
    final sizeBuf = ByteData(4)..setInt32(0, jpegBytes.length, Endian.big);
    socket.add(sizeBuf.buffer.asUint8List());
    socket.add(jpegBytes);
    await socket.flush();
  }

  Future<void> _sendEnd(Socket socket) async {
    final sizeBuf = ByteData(4)..setInt32(0, -1, Endian.big);
    socket.add(sizeBuf.buffer.asUint8List());
    await socket.flush();
  }

  Uint8List _convertToJpeg(CameraImage cameraImage) {
    if (cameraImage.format.group != ImageFormatGroup.yuv420) {
      return Uint8List.fromList(
        img.encodeJpg(
          img.Image.fromBytes(
            width: cameraImage.width,
            height: cameraImage.height,
            bytes: cameraImage.planes[0].bytes.buffer,
            numChannels: 1,
          ),
          quality: 80,
        ),
      );
    }

    final imageWidth = cameraImage.width;
    final imageHeight = cameraImage.height;

    final yBuffer = cameraImage.planes[0].bytes;
    final uBuffer = cameraImage.planes[1].bytes;
    final vBuffer = cameraImage.planes[2].bytes;

    final int yRowStride = cameraImage.planes[0].bytesPerRow;
    final int yPixelStride = cameraImage.planes[0].bytesPerPixel!;

    final int uvRowStride = cameraImage.planes[1].bytesPerRow;
    final int uvPixelStride = cameraImage.planes[1].bytesPerPixel!;

    final image = img.Image(width: imageWidth, height: imageHeight);

    for (int h = 0; h < imageHeight; h++) {
      int uvh = (h / 2).floor();

      for (int w = 0; w < imageWidth; w++) {
        int uvw = (w / 2).floor();

        final yIndex = (h * yRowStride) + (w * yPixelStride);

        // Y plane should have positive values belonging to [0...255]
        final int y = yBuffer[yIndex];

        // U/V Values are subsampled i.e. each pixel in U/V chanel in a
        // YUV_420 image act as chroma value for 4 neighbouring pixels
        final int uvIndex = (uvh * uvRowStride) + (uvw * uvPixelStride);

        // U/V values ideally fall under [-0.5, 0.5] range. To fit them into
        // [0, 255] range they are scaled up and centered to 128.
        // Operation below brings U/V values to [-128, 127].
        final int u = uBuffer[uvIndex];
        final int v = vBuffer[uvIndex];

        // Compute RGB values per formula above.
        int r = (y + v * 1436 / 1024 - 179).round();
        int g = (y - u * 46549 / 131072 + 44 - v * 93604 / 131072 + 91).round();
        int b = (y + u * 1814 / 1024 - 227).round();

        r = r.clamp(0, 255);
        g = g.clamp(0, 255);
        b = b.clamp(0, 255);

        image.setPixelRgb(w, h, r, g, b);
      }
    }

    return Uint8List.fromList(img.encodeJpg(image, quality: 80));
  }

  Future<void> _stopCamera() async {
    try {
      await _sendEnd(_socket);
      await _socket.close();

      if (_controller.value.isStreamingImages) {
        await _controller.stopImageStream();
      }
      await _controller.dispose();

      setState(() {
        _isStreaming = false; // update UI
      });
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Could not stop streaming.')),
      );
    }

    WakelockPlus.disable();
  }

  void _toggleStreaming() async {
    if (_isStreaming) {
      // Already streaming → stop
      await _stopCamera();
      return;
    }

    if (_formKey.currentState!.validate()) {
      CameraDescription? selectedCamera =
          _cameraKey.currentState!._selectedCamera;
      if (selectedCamera == null) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Please select a camera')));
        return;
      }

      if (_cameraKey.currentState!._cameras.isEmpty) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('No cameras available')));
        return;
      }

      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Processing Data')));

      var status = await Permission.camera.status;
      if (!status.isGranted) {
        status = await Permission.camera.request();
        if (!status.isGranted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Camera permission denied')),
          );
          return;
        }
      }

      try {
        //await _initCamera(selectedCamera);
        await _initCameraWithRetry(selectedCamera);

        // Connect using the fields from the form
        final ip = _serverIp.text;
        final port = int.parse(_serverPort.text);
        await _connect(ip, port);

        await _sendName(_socket, _cameraName.text);

        final framesPerInterval = int.parse(_framesPerInterval.text);
        await _sendFramesPerInterval(_socket, framesPerInterval);

        WakelockPlus.enable();

        int targetFps = int.parse(_framerate.text); // desired frame rate
        int frameIntervalMs = (1000 ~/ targetFps);
        int lastSent = 0;

        // Start streaming
        _controller.startImageStream((image) async {
          try {
            int now = DateTime.now().millisecondsSinceEpoch;
            if (now - lastSent < frameIntervalMs) return; // skip this frame
            lastSent = now;

            final jpegBytes = _convertToJpeg(image);
            await _sendFrame(_socket, jpegBytes);
          } catch (e) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Could not send frame.')),
            );
          }
        });

        setState(() {
          _isStreaming = true; // update UI
        });
      } catch (e, stacktrace) {
        print(e.toString());
        print(stacktrace.toString());
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Could not start streaming: $e')),
        );

        setState(() {
          _isStreaming = false; // update UI
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _formKey,
      child: Column(
        children: [
          Padding(
            padding: EdgeInsets.fromLTRB(10, 0, 10, 10),
            child: TextFormField(
              controller: _cameraName,
              decoration: InputDecoration(
                border: OutlineInputBorder(),
                labelText: 'Camera name',
              ),
              keyboardType: TextInputType.text,
              textInputAction: TextInputAction.next,

              validator: (value) {
                if (value == null || value.isEmpty) {
                  return 'Please enter the camera name';
                }
                return null;
              },
            ),
          ),

          Padding(
            padding: EdgeInsets.fromLTRB(10, 0, 10, 10),
            child: TextFormField(
              controller: _framerate,
              decoration: InputDecoration(
                border: OutlineInputBorder(),
                labelText: 'Framerate',
              ),
              keyboardType: TextInputType.text,
              textInputAction: TextInputAction.next,

              validator: (value) {
                if (value == null ||
                    value.isEmpty ||
                    int.tryParse(value) == null ||
                    int.tryParse(value)! <= 0) {
                  return 'Please enter a numeric and positive frames-per-interval';
                }

                return null;
              },
            ),
          ),

          Padding(
            padding: EdgeInsets.fromLTRB(10, 0, 10, 10),
            child: TextFormField(
              controller: _framesPerInterval,
              decoration: InputDecoration(
                border: OutlineInputBorder(),
                labelText: 'Frames per interval',
              ),
              keyboardType: TextInputType.text,
              textInputAction: TextInputAction.next,

              validator: (value) {
                if (value == null ||
                    value.isEmpty ||
                    int.tryParse(value) == null ||
                    int.tryParse(value)! <= 0) {
                  return 'Please enter a numeric and positive frames-per-interval';
                }

                return null;
              },
            ),
          ),

          Padding(
            padding: EdgeInsets.fromLTRB(10, 0, 10, 10),
            child: TextFormField(
              controller: _serverIp,
              decoration: InputDecoration(
                border: OutlineInputBorder(),
                labelText: 'Camera server IP address',
              ),
              keyboardType: TextInputType.text,
              textInputAction: TextInputAction.next,

              validator: (value) {
                if (value == null ||
                    value.isEmpty ||
                    InternetAddress.tryParse(value) == null) {
                  return 'Please enter a valid IP address';
                }

                return null;
              },
            ),
          ),

          Padding(
            padding: EdgeInsets.fromLTRB(10, 0, 10, 10),
            child: TextFormField(
              controller: _serverPort,
              decoration: InputDecoration(
                border: OutlineInputBorder(),
                labelText: 'Camera server port number',
              ),
              keyboardType: TextInputType.text,
              textInputAction: TextInputAction.next,

              validator: (value) {
                if (value == null ||
                    value.isEmpty ||
                    int.tryParse(value) == null ||
                    int.tryParse(value)! <= 0 ||
                    int.tryParse(value)! > 65535) {
                  return 'Please enter a valid port number';
                }

                return null;
              },
            ),
          ),

          Padding(
            padding: EdgeInsets.fromLTRB(10, 0, 10, 10),
            child: CameraList(key: _cameraKey),
          ),

          const SizedBox(height: 10),
          FilledButton(
            onPressed: _toggleStreaming,
            child: Text(_isStreaming ? 'Stop streaming' : 'Start streaming'),
          ),
        ],
      ),
    );
  }
}

class CameraList extends StatefulWidget {
  const CameraList({
    super.key,
    //required this.onCameraSelected,
  });

  //final ValueChanged<CameraDescription?> onCameraSelected;

  @override
  State<CameraList> createState() => _CameraListState();
}

class _CameraListState extends State<CameraList> {
  final List<CameraDescription> _cameras = <CameraDescription>[];
  CameraDescription? _selectedCamera;

  void _getCameras() async {
    try {
      final cameras = await availableCameras();
      setState(() {
        _cameras.clear();
        _cameras.addAll(cameras);
        if (_cameras.isNotEmpty) {
          _selectedCamera = _cameras.first;
        }
      });
    } catch (e) {
      print('Error getting cameras: $e');
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Error accessing cameras: $e')));
    }
  }

  @override
  void initState() {
    super.initState();
    _getCameras();
  }

  @override
  Widget build(BuildContext context) {
    return DropdownMenu(
      dropdownMenuEntries:
          _cameras.map((CameraDescription camera) {
            return DropdownMenuEntry(
              value: camera,
              label: '(${camera.name} - ${camera.lensDirection.name})',
            );
          }).toList(),

      onSelected: (CameraDescription? camera) {
        setState(() {
          _selectedCamera = camera;
        });
      },

      initialSelection: _selectedCamera,
      width: double.infinity,
      label: const Text('Select a camera'),
      inputDecorationTheme: const InputDecorationTheme(
        border: OutlineInputBorder(),
      ),
    );
  }
}
