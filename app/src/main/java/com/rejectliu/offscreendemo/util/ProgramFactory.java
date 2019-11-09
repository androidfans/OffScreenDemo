package com.rejectliu.offscreendemo.util;


public class ProgramFactory {

    public static IProgram create(String vertexShaderCode, String fragmentShaderCode) {
        return new Program(vertexShaderCode, fragmentShaderCode);
    }
}
