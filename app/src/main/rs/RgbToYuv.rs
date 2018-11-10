#pragma version(1)
#pragma rs java_package_name(ru.cherryperry.instavideo.renderscript)

rs_allocation yuvAllocation;
int width;
int height;

void __attribute__((kernel)) convert(uchar4 v_in, uint32_t x, uint32_t y) {
    int32_t yValue = ((66 * v_in.r + 129 * v_in.g + 25 * v_in.b + 128) >> 8) + 16;
    uint32_t yPosition = y * width + x;
    rsSetElementAt_uchar(yuvAllocation, clamp(yValue, 0, 255), yPosition);
    if (x % 2 == 0 && y % 2 == 0) {
        int32_t vValue = ((112 * v_in.r - 94 * v_in.g - 18 * v_in.b + 128) >> 8) + 128;
        uint32_t vPosition = width * height + y / 2 * width + x;
        rsSetElementAt_uchar(yuvAllocation, clamp(vValue, 0, 255), vPosition);
        int32_t uValue = ((-38 * v_in.r - 74 * v_in.g + 112 * v_in.b + 128) >> 8) + 128;
        uint32_t uPosition = vPosition + 1;
        rsSetElementAt_uchar(yuvAllocation, clamp(uValue, 0, 255), uPosition);
    }
}
