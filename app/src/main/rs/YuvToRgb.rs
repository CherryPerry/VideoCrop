#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(ru.cherryperry.instavideo.data.media.renderscript)

rs_allocation yuv;
ushort width;
ushort height;
ushort type;

uchar4 RS_KERNEL convert(uint32_t x, uint32_t y) {
    uint3 yuv_p;
    if (type == 0) {
        // YUV420P
        yuv_p.s0 = y * width + x;
        yuv_p.s1 = y / 2 * width / 2 + x / 2 + width * height;
        yuv_p.s2 = y / 2 * width / 2 + x / 2 + width * height + width * height / 4;
    }
    if (type == 1) {
        // TODO Packed
        yuv_p.s0 = y * width + x;
        yuv_p.s1 = yuv_p.s0;
        yuv_p.s2 = yuv_p.s0;
    }
    if (type == 2) {
        // YUV420SP NV12
        yuv_p.s0 = y * width + x;
        yuv_p.s1 = width * height + y / 2 * width + (x & ~1);
        yuv_p.s2 = yuv_p.s1 + 1;
    }
    if (type == 3) {
        // YUV420SP NV21
        yuv_p.s0 = y * width + x;
        yuv_p.s2 = width * height + y / 2 * width + (x & ~1);
        yuv_p.s1 = yuv_p.s2 + 1;
    }

    uchar3 yuv_v;
    yuv_v.s0 = rsGetElementAt_uchar(yuv, yuv_p.s0);
    yuv_v.s1 = rsGetElementAt_uchar(yuv, yuv_p.s1);
    yuv_v.s2 = rsGetElementAt_uchar(yuv, yuv_p.s2);
    return rsYuvToRGBA_uchar4(yuv_v.s0, yuv_v.s1, yuv_v.s2);
}
