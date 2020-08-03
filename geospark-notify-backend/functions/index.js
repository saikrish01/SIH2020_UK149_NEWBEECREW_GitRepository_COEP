const functions = require("firebase-functions");
const cors = require("cors")({ origin: true });
const admin = require("firebase-admin");
admin.initializeApp();
const database = admin.database().ref("/geofences");
const udatabase = admin.database().ref("/users");

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

exports.addgeofence = functions.https.onRequest((req, res) => {
  return cors(req, res, () => {
    if (req.method !== "POST") {
      return res.status(401).json({
        message: "Not allowed"
      });
    }
    console.log(req.body);
    const geofence_id = req.body.geofence_id;
    const gtitle = req.body.gtitle;
    const gmessage = req.body.gmessage;
    database.push({ geofence_id, gtitle, gmessage });
    res.status(200).json({
      message: "Geofence created"
    });
  });
});

exports.triggernotification = functions.https.onRequest((req, res) => {
  return cors(req, res, () => {
    if (req.method !== "POST") {
      return res.status(401).json({
        message: "Not allowed"
      });
    }
    console.log(req.body);
    const geofence_id = req.body.geofence_id;
    const event_type = req.body.event_type;
    const geospark_token = req.body.user_id;

    // database.orderByChild("geofence_id").equalTo(geofence_id).on("child_added", function(snapshot) {
    //  console.log(snapshot.key);
    // });
    if (event_type == "entry") {
      var gtitle, gmessage;
      database
        .orderByChild("geofence_id")
        .equalTo(geofence_id)
        .on(
          "value",
          snapshot => {
            snapshot.forEach(geofence => {
              console.log(geofence.val().gtitle);
              gtitle = geofence.val().gtitle;
              gmessage = geofence.val().gmessage;
              udatabase
                .orderByChild("geospark_token")
                .equalTo(geospark_token)
                .on(
                  "value",
                  snapshot => {
                    snapshot.forEach(user => {
                      console.log(user.val().device_token);
                      var message = {
                        notification: {
                          title: gtitle,
                          body: gmessage
                        },
                        token: user.val().device_token
                      };
                      admin
                        .messaging()
                        .send(message)
                        .then(response => {
                          console.log("Successfully sent message:", response);
                        })
                        .catch(error => {
                          console.log("Error sending message:", error);
                        });
                    });
                  },
                  error => {
                    res.status(error.code).json({
                      message: `Something went wrong. ${error.message}`
                    });
                  }
                );
            });
          },
          error => {
            res.status(error.code).json({
              message: `Something went wrong. ${error.message}`
            });
          }
        );
    }

    res.status(200).json({
      message: "Notification triggered"
    });
  });
});
