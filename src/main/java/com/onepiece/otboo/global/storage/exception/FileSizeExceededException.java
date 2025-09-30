package com.onepiece.otboo.global.storage.exception;

import com.onepiece.otboo.global.exception.ErrorCode;
import com.onepiece.otboo.global.exception.GlobalException;
import java.util.Map;

public class FileSizeExceededException extends GlobalException {

    public FileSizeExceededException(long size, long maxSize) {
        super(ErrorCode.FILE_SIZE_EXCEED,
            Map.of("size", size,
                "maxSize", maxSize));
    }
}
