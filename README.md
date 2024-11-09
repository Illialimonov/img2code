# Img2Code 
![Img2Code Logo](https://img2code.xyz/_next/image?url=%2Fstatic%2Fimages%2Ffavicon.png&w=96&q=75)


## Project Overview
**Img2Code** is an innovative application that allows users to extract programming code from images using advanced AI technology powered by Gemini. The application is designed to make code extraction seamless, efficient, and accessible, offering a set of unique functionalities and a structured subscription model to meet diverse user needs.

## Key Functionalities
- **Image-to-Code Conversion**: Quickly converts photos of programming code into editable text, allowing for easy modifications and reuse.
- **In-Comments Feature**: Automatically wraps the extracted code in comments, providing context and structure around each code block for better readability.
- **Personal Space**: Users can manage their subscriptions and access a history of previous conversions in a dedicated personal dashboard.

## Daily Credits & Browser Fingerprinting
Upon entering the site, each user is granted **5 daily credits**, replenished every 24 hours. Credits are carefully managed through browser fingerprinting, ensuring users cannot manipulate their credits by opening new tabs or using different browsers.

## Subscription Tiers
Img2Code offers three subscription options, each tailored to specific needs and requiring authentication either manually or via OAuth2 with Google or GitHub:

1. **Free Tier**:
   - **Credits**: 10 initial credits upon subscription.
   - **Access**: Basic image-to-code conversion.

2. **Premium Tier**:
   - **Credits**: Unlimited daily conversions.
   - **File Size**: Up to 5MB per file.
   - **Features**: Includes Code-in-Comments, conversion history access, and faster conversion times (approximately 7 seconds per image).

3. **Pro Tier**:
   - **Credits**: Unlimited daily conversions.
   - **File Size**: Up to 10MB per file.
   - **Features**: All Premium features plus the fastest conversions (around 3 seconds per image).

## My Contribution to Img2Code
As a **Project Lead and Backend Developer** for Img2Code, I contributed to both the strategic direction and technical implementation of the application. I led the backend development using Java **Spring** as the primary framework, implementing **Spring Security** for secure user authentication and **Spring Data JPA** to manage application entities. I utilized **MongoDB** as the database for scalable data storage and retrieval, ensuring efficient handling of user and conversion data.

For AI-driven code extraction, I integrated **Vertex AI API**, enabling accurate image processing and code conversion. Additionally, I managed payment processing and subscription handling using the **Stripe API**, offering users seamless payment options for subscription upgrades. Task management and project coordination were organized in **Atlassian Kanban**, where I facilitated sprint planning and task tracking to keep the team aligned and on schedule.

For cloud services, I chose **Google Cloud because** it provides a simple, beginner-friendly interface to achieve my goals. I deployed my server using **App Engine** and utilized **Cloud Storage** to store information like code snippets for efficient retrieval. This setup enabled scalable and efficient handling of code samples and images, supporting key application functions with ease.

## Application Interface

Landing page:
![code1](https://github.com/user-attachments/assets/b29b2298-e93e-491e-bc85-2bf3f3808b44)

Convertation page (empty):
![code2](https://github.com/user-attachments/assets/0c24c3e5-6d28-4edb-8ffd-6c7f20a2e72b)

Convertation page (result included)
![code3](https://github.com/user-attachments/assets/544e12c0-d83c-4e60-9294-243afe754dfd)

Pricing page:
![code4](https://github.com/user-attachments/assets/465ada15-00c2-4b20-9f87-14652737c665)

Personal space (light theme):
![code5](https://github.com/user-attachments/assets/af9b5a13-0c80-40f9-bcfd-c7b7b58d9b24)

History log:
![code6](https://github.com/user-attachments/assets/66389fed-8982-41be-a6a6-c3895d63ce01)

