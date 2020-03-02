## Video crop app

[![Build Status](https://travis-ci.com/CherryPerry/VideoCrop.svg?branch=master)](https://travis-ci.com/CherryPerry/VideoCrop)
[![Version](https://img.shields.io/github/release/CherryPerry/VideoCrop.svg)](https://github.com/CherryPerry/VideoCrop/releases)
[![License](https://img.shields.io/badge/license-WTFPL-green.svg)](https://github.com/CherryPerry/VideoCrop/blob/master/LICENSE)

**UPDATE:** Actually, used approach is not the best one. Consider using [Surface](https://developer.android.com/reference/android/media/MediaCodec#using-an-output-surface) approach for both decoder and encoder. Doing it this way, you can get predictable result before crop, using sufrace for rendering video in UI, and after crop using surface for encode it. I created this project just to check, if it is possible to use `ByteBuffer`s instead. Also audio [does not work](https://github.com/CherryPerry/VideoCrop/issues/1) for some reason...

You can crop video to make it square and be less than 20 seconds in duration.

It uses [MediaCodec](https://developer.android.com/reference/android/media/MediaCodec) API for decoding and encoding video.
Source video file is read by [MediaExtractor](https://developer.android.com/reference/android/media/MediaExtractor).
Frame transformations are done with regular [Bitmap](https://developer.android.com/reference/android/graphics/Bitmap).
To be able to do so, it is required to convert frame's pixel array from YUV to RGB and back
with help of [RenderScript](https://developer.android.com/guide/topics/renderscript/compute).
Frames are saved to file with help of [MediaMuxer](https://developer.android.com/reference/android/media/MediaMuxer) after encoding.

#### Screenshots

![screenshot_20181117-134723](https://user-images.githubusercontent.com/9081555/48660387-d1641000-ea71-11e8-9875-ff7b421971ea.png)
![screenshot_20181117-134733](https://user-images.githubusercontent.com/9081555/48660388-d1641000-ea71-11e8-80c6-1cae76087921.png)
![screenshot_20181117-134741](https://user-images.githubusercontent.com/9081555/48660389-d1fca680-ea71-11e8-80a8-c2fba443fefe.png)
![screenshot_20181117-134814](https://user-images.githubusercontent.com/9081555/48660390-d1fca680-ea71-11e8-96b1-0c3a0d6492f1.png)
![screenshot_20181117-135409](https://user-images.githubusercontent.com/9081555/48660386-d1641000-ea71-11e8-9fc5-5926f132e95b.png)

#### License

[WTFPL](https://github.com/CherryPerry/VideoCrop/blob/master/LICENSE) because I don't even know if this code works on devices other than mine.
