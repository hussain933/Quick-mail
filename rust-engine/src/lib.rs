use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jobjectArray};
use std::ffi::CString;

mod smtp;
mod auth;
mod validation;
mod attachment;

#[no_mangle]
pub extern "system" fn Java_com_quickmail_rust_RustBridge_sendEmail(
    mut env: JNIEnv,
    _class: JClass,
    to: JString,
    subject: JString,
    body: JString,
    attachments: jobjectArray,
    sender_email: JString,
    sender_password: JString,
) -> jboolean {
    let to: String = env.get_string(to).unwrap().into();
    let subject: String = env.get_string(subject).unwrap().into();
    let body: String = env.get_string(body).unwrap().into();
    let email: String = env.get_string(sender_email).unwrap().into();
    let password: String = env.get_string(sender_password).unwrap().into();

    // Convert Java String array to Vec<String>
    let len = env.get_array_length(attachments).unwrap() as usize;
    let mut paths: Vec<String> = Vec::with_capacity(len);
    for i in 0..len {
        let obj = env.get_object_array_element(attachments, i as i32).unwrap();
        let jstr: JString = obj.into();
        paths.push(env.get_string(jstr).unwrap().into());
    }

    match smtp::send_mail(&to, &subject, &body, &paths, &email, &password) {
        Ok(()) => 1,
        Err(_) => 0,
    }
}
