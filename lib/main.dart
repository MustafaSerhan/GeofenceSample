import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:location/location.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      debugShowCheckedModeBanner: false,
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  double height, width;
  Completer<GoogleMapController> _controller = Completer();
  double lat, lon;
  MethodChannel channel;
  double radius = 0;
  Set<Circle> circles = Set<Circle>();
  Set<Marker> markers = Set<Marker>();
  var location = Location();
  String text = "Ekle";
  StreamSubscription<LocationData> locationSubscription;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    channel = new MethodChannel("GeofenceChannel");
    startLocation();
  }

  @override
  void dispose() {
    // TODO: implement dispose
    locationSubscription.cancel();
    locationSubscription = null;

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    height = MediaQuery.of(context).size.height;
    width = MediaQuery.of(context).size.width;
    return Scaffold(
      body: Container(
        height: height,
        width: width,
        color: Colors.purple,
        child: Center(
          child: Stack(
            children: <Widget>[
              GoogleMap(
                mapType: MapType.normal,
                initialCameraPosition: _kGooglePlex,
                onMapCreated: (GoogleMapController controller) {
                  _controller.complete(controller);
                },
                onTap: tapMethod,
                circles: circles,
                markers: markers,
              ),
              Positioned(
                bottom: 20,
                left: width * .05,
                child: Container(
                    width: width * .9,
                    padding: EdgeInsets.all(8.0),
                    //color: Colors.green,
                    child: Column(
                      children: <Widget>[
                        Slider(
                          value: radius,
                          activeColor: Colors.purple,
                          max: 1000,
                          min: 0,
                          label: "${radius.toInt()} Metre",
                          divisions: 20,
                          onChanged: (double value) async {
                            radius = value;
                            if (lat != null) {
                              circles = Set.from([
                                Circle(
                                    circleId: CircleId("deneme"),
                                    center: LatLng(lat, lon),
                                    radius: radius,
                                    fillColor:
                                        Colors.blueAccent[200].withOpacity(.2),
                                    strokeWidth: 2,
                                    strokeColor: Colors.blue)
                              ]);

                              GoogleMapController controller = await _controller.future;
                              setZoom(controller);
                            }

                            setState(() {});
                          },
                        ),
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: <Widget>[
                            Container(
                              color: Colors.white,
                              width: width * .2,
                              padding: EdgeInsets.all(15.0),
                              child: Text("Enlem: ${lat}"),
                            ),
                            Container(
                              color: Colors.white,
                              width: width * .2,
                              padding: EdgeInsets.all(15.0),
                              child: Text(
                                "Boylam: ${lon}",
                              ),
                            ),
                            GestureDetector(
                              
                              onTap: () async {
                                text  = "Eklendi";
                                setState(() {});
                                Future.delayed(Duration(seconds: 1), (){
                                  text = "Ekle";
                                  setState(() {});
                                });
                                await channel.invokeMethod("addGeofence",
                                    {"lat": lat, "lon": lon, "radius": radius});
                              },
                              child: Container(
                                height: 50,
                                width: 120,
                                color: Colors.deepPurple[200],
                                alignment: Alignment.center,
                                child: Text(text),
                              ),
                            ),
                          ],
                        ),
                      ],
                    )),
              ),
            ],
          ),
        ),
      ),
    );
  }

  static final CameraPosition _kGooglePlex = CameraPosition(
    target: LatLng(41.0862217, 29.1722575),
    zoom: 10.29,
  );

  void tapMethod(LatLng argument) async {
    GoogleMapController controller = await _controller.future;
    lat = argument.latitude;
    lon = argument.longitude;
    setZoom(controller);

    circles = Set.from([
      Circle(
          circleId: CircleId("deneme"),
          center: argument,
          radius: radius,
          fillColor: Colors.blueAccent[200].withOpacity(.2),
          strokeWidth: 2,
          strokeColor: Colors.blue)
    ]);

    markers.add(Marker(
      markerId: MarkerId("deneme"),
      position: argument,
    ));

    setState(() {});
  }

  void startLocation() async {
    await channel.invokeMethod("servisBaslat");

    try {
      bool permission = await location.hasPermission();
      //bool service = await location.serviceEnabled();
      debugPrint("İzin: ${permission}");

      if (permission) {
        locationSubscription =
            location.onLocationChanged().listen((LocationData data) {
          markers.add(Marker(
              markerId: MarkerId("me"),
              position: LatLng(data.latitude, data.longitude)));
          setState(() {});
        });
      }
    } catch (e) {}
  }

  void setZoom(GoogleMapController controller) {
    if (radius <= 250) {
      controller.animateCamera(
        CameraUpdate.newCameraPosition(
          CameraPosition(
            target: LatLng(lat, lon),
            zoom: 16.4,
          ),
        ),
      );
    } else if (radius <= 500) {
      controller.animateCamera(
        CameraUpdate.newCameraPosition(
          CameraPosition(
            target: LatLng(lat, lon),
            zoom: 15,
          ),
        ),
      );
    } else if (radius <= 750) {
      controller.animateCamera(
        CameraUpdate.newCameraPosition(
          CameraPosition(
            target: LatLng(lat, lon),
            zoom: 14.5,
          ),
        ),
      );
    } else {
      controller.animateCamera(
        CameraUpdate.newCameraPosition(
          CameraPosition(
            target: LatLng(lat, lon),
            zoom: 14,
          ),
        ),
      );
    }
  }
}
