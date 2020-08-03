import os
import tensorflow.compat.v1 as tf
import cv2
import sys
import numpy as np

sys.path.append("..")
# sys.path.append('E:\\somesh\\sai\\Tensorflow\\models\\research')
from utils import label_map_util
from utils import visualization_utils as vis_util

MODEL_NAME = 'ifrozen_nference_graph'
IMAGE_NAME = 'test1.jpg'

PATH_TO_CKPT = 'C:\\Users\\Sai Krishna .M\\Tensorflow\\models\\research\\object_detection\\inference_graph\\frozen_inference_graph.pb'
PATH_TO_LABELS = 'C:\\Users\\Sai Krishna .M\\Tensorflow\\models\\research\\object_detection\\training\\label_map.pbtxt'

PATH_TO_IMAGE = 'E:\\somesh\\sai\\Tensorflow\\models\\research\\object_detection\\5.JPG'
PATH_TO_VIDEO = 'test3.MP4'

NUM_CLASSES = 5

label_map = label_map_util.load_labelmap(PATH_TO_LABELS)
categories = label_map_util.convert_label_map_to_categories(label_map, max_num_classes=NUM_CLASSES, use_display_name=True)
category_index = label_map_util.create_category_index(categories)

detection_graph = tf.Graph()
with detection_graph.as_default():
    od_graph_def = tf.GraphDef()
    with tf.gfile.GFile(PATH_TO_CKPT, 'rb') as fid:
        serialized_graph = fid.read()
        od_graph_def.ParseFromString(serialized_graph)
        tf.import_graph_def(od_graph_def, name='')

    sess = tf.Session(graph=detection_graph)

image_tensor = detection_graph.get_tensor_by_name('image_tensor:0')
detection_boxes = detection_graph.get_tensor_by_name('detection_boxes:0')
detection_scores = detection_graph.get_tensor_by_name('detection_scores:0')
detection_classes = detection_graph.get_tensor_by_name('detection_classes:0')

num_detections = detection_graph.get_tensor_by_name('num_detections:0')

# image = cv2.imread(PATH_TO_IMAGE)
# image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
# image_expanded = np.expand_dims(image_rgb, axis=0)
# (boxes, scores, classes, num) = sess.run(
#     [detection_boxes, detection_scores, detection_classes, num_detections],
#     feed_dict={image_tensor: image_expanded})


# vis_util.visualize_boxes_and_labels_on_image_array(
#     image,
#     np.squeeze(boxes),
#     np.squeeze(classes).astype(np.int32),
#     np.squeeze(scores),
#     category_index,
#     use_normalized_coordinates=True,
#     line_thickness=8,
#     min_score_thresh=0.60)


# cv2.imshow('Object detector', image)

# # Press any key to close the image
# cv2.waitKey(0)

# # Clean up
# cv2.destroyAllWindows()

video = cv2.VideoCapture(PATH_TO_VIDEO)
while(video.isOpened()):

    # Acquire frame and expand frame dimensions to have shape: [1, None, None, 3]
    # i.e. a single-column array, where each item in the column has the pixel RGB value
    ret, frame = video.read()
    frame = cv2.rotate(frame, cv2.ROTATE_90_CLOCKWISE)
    frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
    frame_expanded = np.expand_dims(frame_rgb, axis=0)

    # Perform the actual detection by running the model with the image as input
    (boxes, scores, classes, num) = sess.run(
        [detection_boxes, detection_scores, detection_classes, num_detections],
        feed_dict={image_tensor: frame_expanded})

    # Draw the results of the detection (aka 'visulaize the results')
    vis_util.visualize_boxes_and_labels_on_image_array(
        frame,
        np.squeeze(boxes),
        np.squeeze(classes).astype(np.int32),
        np.squeeze(scores),
        category_index,
        use_normalized_coordinates=True,
        line_thickness=8,
        min_score_thresh=0.60)

    # All the results have been drawn on the frame, so it's time to display it.
    cv2.imshow('Object detector', frame)

    # Press 'q' to quit
    if cv2.waitKey(1) == ord('q'):
        break
# Clean up
video.release()
cv2.destroyAllWindows()