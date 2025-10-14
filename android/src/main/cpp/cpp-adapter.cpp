#include <jni.h>
#include "playagerangedeclarationOnLoad.hpp"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
  return margelo::nitro::playagerangedeclaration::initialize(vm);
}
