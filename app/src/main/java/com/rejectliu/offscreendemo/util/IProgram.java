package com.rejectliu.offscreendemo.util;

public interface IProgram {

    void create(String vertexShaderCode, String fragmentShaderCode);

    void delete();

    int id();
}
