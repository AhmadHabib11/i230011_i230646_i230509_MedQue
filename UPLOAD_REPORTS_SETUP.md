# MedQue - Upload Reports Setup Guide

## Overview
The Upload Reports feature has been converted from static/hardcoded to fully dynamic with database integration and file storage.

## What Was Changed

### 1. Database Setup
- **New Table**: `reports` table added to `medque_db` database
- **Fields**:
  - `id`: Auto-increment primary key
  - `user_id`: Foreign key to users table
  - `report_name`: Original filename
  - `file_path`: Server path to stored file
  - `file_size`: Formatted file size (e.g., "1.5 MB")
  - `file_type`: MIME type of the file
  - `uploaded_at`: Timestamp of upload

### 2. PHP APIs Created

#### a) `upload_report.php`
- Handles file uploads from Android app
- Supports base64 encoded files (from app) and traditional file uploads
- Validates file types: PDF, JPG, JPEG, PNG, DOC, DOCX
- Maximum file size: 10MB
- Stores files in `uploads/reports/` directory
- Returns upload status and file information

#### b) `get_reports.php`
- Retrieves all reports for a specific user
- Returns report list with file URLs
- Supports both GET and POST requests

### 3. Android App Changes

#### Upload_reports.kt Features:
- **File Picker**: Users can select files from their device
- **File Preview**: Shows selected file name and size before upload
- **Remove File**: Option to deselect a file
- **API Integration**: Uploads file to server using Volley
- **Dynamic UI**: Shows/hides file card based on selection
- **User Authentication**: Gets user_id from SharedPreferences

## Setup Instructions

### Step 1: Database Setup
1. Open phpMyAdmin (http://localhost/phpmyadmin)
2. Select the `medque_db` database
3. Run the SQL script from `create_reports_table.sql`:
   ```sql
   CREATE TABLE IF NOT EXISTS reports (
       id INT(11) AUTO_INCREMENT PRIMARY KEY,
       user_id INT(11) NOT NULL,
       report_name VARCHAR(255) NOT NULL,
       file_path VARCHAR(500) NOT NULL,
       file_size VARCHAR(50) NOT NULL,
       file_type VARCHAR(100) NOT NULL,
       uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
   ```

### Step 2: Create Upload Directory
1. Navigate to: `c:\xampp\htdocs\medque_app\`
2. Create folder structure: `uploads\reports\`
3. Set folder permissions (if on Linux/Mac):
   ```bash
   chmod 777 uploads/reports/
   ```

### Step 3: Update API URL (if needed)
If not using Android Emulator, update the URL in `Upload_reports.kt`:
- For Emulator: `http://10.0.2.2/medque_app/upload_report.php` (current)
- For Real Device: `http://YOUR_LOCAL_IP/medque_app/upload_report.php`
  - Find your IP: Run `ipconfig` in Windows CMD and look for IPv4 Address
  - Example: `http://192.168.1.100/medque_app/upload_report.php`

### Step 4: Test the Feature
1. Start XAMPP (Apache and MySQL)
2. Run the Android app
3. Navigate to Upload Reports screen
4. Click on the upload area to select a file
5. Choose a PDF, image, or document file
6. View the file preview with name and size
7. Click "Submit Report" to upload
8. Check `uploads/reports/` folder for the uploaded file
9. Verify database entry in phpMyAdmin

## File Flow

```
User Selects File → Upload_reports.kt
         ↓
File converted to Base64
         ↓
POST to upload_report.php
         ↓
File decoded and saved to uploads/reports/
         ↓
Record inserted into reports table
         ↓
Success response returned
         ↓
User redirected to Home
```

## API Endpoints

### Upload Report
- **URL**: `http://10.0.2.2/medque_app/upload_report.php`
- **Method**: POST
- **Parameters**:
  - `user_id`: User ID (integer)
  - `file_name`: Original filename (string)
  - `file_data`: Base64 encoded file (string)
- **Response**:
  ```json
  {
    "success": true,
    "message": "Report uploaded successfully",
    "data": {
      "report_id": 1,
      "file_name": "Lab_Report.pdf",
      "file_size": "1.2 MB",
      "file_path": "uploads/reports/abc123_1234567890.pdf",
      "uploaded_at": "2025-12-03 10:30:00"
    }
  }
  ```

### Get Reports
- **URL**: `http://10.0.2.2/medque_app/get_reports.php`
- **Method**: POST or GET
- **Parameters**:
  - `user_id`: User ID (integer)
- **Response**:
  ```json
  {
    "success": true,
    "message": "Reports fetched successfully",
    "data": {
      "reports": [
        {
          "id": 1,
          "report_name": "Lab_Report.pdf",
          "file_path": "uploads/reports/abc123_1234567890.pdf",
          "file_url": "http://localhost/medque_app/uploads/reports/abc123_1234567890.pdf",
          "file_size": "1.2 MB",
          "file_type": "application/pdf",
          "uploaded_at": "2025-12-03 10:30:00"
        }
      ],
      "count": 1
    }
  }
  ```

## Supported File Types
- PDF (.pdf)
- Images (.jpg, .jpeg, .png)
- Word Documents (.doc, .docx)

## Troubleshooting

### Issue: File upload fails
- Check if `uploads/reports/` directory exists
- Verify folder has write permissions
- Check file size (must be under 10MB)
- Ensure file type is supported

### Issue: "User not logged in" error
- Make sure user_id is stored in SharedPreferences during login
- Check if SharedPreferences key is "user_id"

### Issue: Cannot connect to server
- Verify XAMPP is running (Apache and MySQL)
- Check API URL is correct (10.0.2.2 for emulator)
- For real device, use computer's local IP address
- Ensure `android:usesCleartextTraffic="true"` in AndroidManifest

### Issue: Database error
- Run the SQL script to create reports table
- Verify foreign key constraint with users table
- Check database connection in config.php

## Next Steps (Optional Enhancements)
1. Add a screen to view all uploaded reports
2. Add delete report functionality
3. Add progress bar during upload
4. Support for multiple file selection
5. Image preview for uploaded images
6. Download/share uploaded reports

## Files Modified/Created
1. **Created**: `c:\xampp\htdocs\medque_app\create_reports_table.sql`
2. **Created**: `c:\xampp\htdocs\medque_app\upload_report.php`
3. **Created**: `c:\xampp\htdocs\medque_app\get_reports.php`
4. **Modified**: `Upload_reports.kt` - Added complete dynamic functionality
5. **Permissions**: Already present in AndroidManifest.xml

## Notes
- File names are made unique using `uniqid()` and timestamp to prevent overwrites
- Base64 encoding is used for file transfer from Android to PHP
- SharedPreferences is used to get logged-in user's ID
- All API responses follow JSON format with success/error handling
