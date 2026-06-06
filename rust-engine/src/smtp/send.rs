use lettre::message::{Mailbox, Message, Attachment};
use lettre::transport::smtp::authentication::Credentials;
use lettre::SmtpTransport;
use lettre::Transport;
use std::path::Path;
use std::fs;

pub fn send_mail(
    to: &str,
    subject: &str,
    body: &str,
    attachment_paths: &[String],
    sender_email: &str,
    sender_password: &str,
) -> Result<(), Box<dyn std::error::Error>> {
    let from: Mailbox = sender_email.parse()?;
    let to: Mailbox = to.parse()?;

    let mut msg = Message::builder()
        .from(from)
        .to(to)
        .subject(subject);

    // Attachments (lettre 0.11 way)
    for path in attachment_paths {
        let data = fs::read(path)?;
        let filename = Path::new(path).file_name().unwrap().to_str().unwrap();
        // Simple attachment with content (MIME type guessed from extension)
        msg = msg.attachment(Attachment::new(filename).body(data, "application/octet-stream".parse()?))?;
    }

    let email = msg.body(body.to_string())?;

    let creds = Credentials::new(sender_email.to_string(), sender_password.to_string());
    let mailer = SmtpTransport::starttls_relay("smtp.gmail.com")?
        .credentials(creds)
        .build();

    mailer.send(&email)?;
    Ok(())
}
