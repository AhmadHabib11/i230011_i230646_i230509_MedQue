# MedQue Upload Reports - Complete Flow Documentation

## 📱 User Journey Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Upload Reports Screen                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  [← Back]              Upload Report                         │
│                                                               │
│  Upload Medical Report                                       │
│  Attach your latest medical report before your appointment.  │
│                                                               │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                                                         │  │
│  │            📄  Tap to upload or drag a file here       │  │
│  │                                                         │  │
│  └───────────────────────────────────────────────────────┘  │
│                         (Click to Select File)               │
│                                                               │
│  After File Selected:                                        │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  📄  Lab_Report_Jan_2024.pdf              [X Remove]  │  │
│  │      1.2 MB                                            │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                               │
│                    [Submit Report Button]                    │
└─────────────────────────────────────────────────────────────┘
```

## 🔄 Technical Flow

### 1. File Selection
```
User taps upload area
    ↓
openFilePicker() called
    ↓
Intent.ACTION_GET_CONTENT launched
    ↓
User selects file from device
    ↓
filePickerLauncher receives result
    ↓
handleSelectedFile(uri) processes file
    ↓
Get file name & size from ContentResolver
    ↓
Update UI: Show file card with details
```

### 2. File Upload
```
User taps "Submit Report"
    ↓
uploadReport() called
    ↓
Check if file is selected
    ↓
Get user_id from SharedPreferences
    ↓
Read file content as InputStream
    ↓
Convert to ByteArray
    ↓
Encode as Base64 string
    ↓
Create Volley StringRequest
    ↓
POST to upload_report.php
    ↓
PHP receives request
```

### 3. Server Processing (upload_report.php)
```
Receive POST request
    ↓
Validate user_id
    ↓
Check if file_data exists (Base64)
    ↓
Decode Base64 to binary
    ↓
Validate file type & size
    ↓
Create uploads/reports/ if needed
    ↓
Generate unique filename
    ↓
Save file to server
    ↓
Calculate formatted file size
    ↓
Insert record to database
    ↓
Return JSON response
```

### 4. Response Handling
```
PHP sends JSON response
    ↓
Volley receives response
    ↓
Parse JSON
    ↓
If success:
    - Show success toast
    - Clear selected file
    - Navigate to Home
    ↓
If error:
    - Show error message
    - Keep file selected
    - Allow retry
```

## 💾 Database Interactions

### Upload Flow
```
Android App                    PHP Server                  MySQL Database
    |                              |                              |
    |--[POST with Base64 file]---->|                              |
    |                              |                              |
    |                              |--[INSERT INTO reports]------>|
    |                              |                              |
    |                              |<----[Return insert_id]-------|
    |                              |                              |
    |<---[JSON success response]---|                              |
    |                              |                              |
```

### Retrieve Flow
```
Android App                    PHP Server                  MySQL Database
    |                              |                              |
    |--[POST with user_id]-------->|                              |
    |                              |                              |
    |                              |--[SELECT FROM reports]------>|
    |                              |                              |
    |                              |<----[Return records]---------|
    |                              |                              |
    |<---[JSON with reports]-------|                              |
    |                              |                              |
```

## 🗂️ File System Structure

```
c:\xampp\htdocs\medque_app\
│
├── config.php                 (Database config & helpers)
├── conn.php                   (Legacy connection file)
├── login.php                  (User login API)
├── signup.php                 (User signup API)
├── update_profile.php         (Profile update API)
├── upload_image.php           (Profile image upload)
│
├── upload_report.php          ✨ NEW - Report upload API
├── get_reports.php            ✨ NEW - Get reports API
├── delete_report.php          ✨ NEW - Delete report API
├── test_reports.html          ✨ NEW - Testing interface
├── create_reports_table.sql   ✨ NEW - SQL setup script
│
└── uploads/
    ├── images/                (Profile pictures)
    └── reports/               ✨ NEW - Medical reports
        ├── 67891abc_1733234567.pdf
        ├── 67892def_1733234789.jpg
        └── ...
```

## 📊 Data Flow Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                        ANDROID APP                            │
│                                                                │
│  ┌────────────────┐    ┌──────────────┐   ┌───────────────┐ │
│  │ Upload_reports │───▶│  File Picker │──▶│ Selected File │ │
│  │   Activity     │    │              │   │   URI + Info  │ │
│  └────────────────┘    └──────────────┘   └───────────────┘ │
│          │                                         │          │
│          │ [Submit Report]                         │          │
│          ▼                                         │          │
│  ┌────────────────┐                               │          │
│  │  Read File &   │◀──────────────────────────────┘          │
│  │ Convert Base64 │                                           │
│  └────────────────┘                                           │
│          │                                                     │
│          │ [POST Request]                                     │
└──────────┼─────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────┐
│                        PHP SERVER                             │
│                                                                │
│  ┌────────────────┐    ┌──────────────┐   ┌───────────────┐ │
│  │ upload_report  │───▶│ Decode Base64│──▶│  Validate &   │ │
│  │     .php       │    │              │   │  Save File    │ │
│  └────────────────┘    └──────────────┘   └───────────────┘ │
│          │                                         │          │
│          │                                         │          │
│          ▼                                         ▼          │
│  ┌────────────────┐                      ┌───────────────┐  │
│  │  Send JSON     │◀─────────────────────│ Insert Record │  │
│  │   Response     │                      │   to DB       │  │
│  └────────────────┘                      └───────────────┘  │
│          │                                         │          │
└──────────┼─────────────────────────────────────────┼──────────┘
           │                                         │
           ▼                                         ▼
┌──────────────────────────────────────────────────────────────┐
│                    MYSQL DATABASE                             │
│                                                                │
│  ┌────────────────────────────────────────────────────────┐  │
│  │  reports Table                                          │  │
│  ├─────────────────────────────────────────────────────────┤ │
│  │  id  │ user_id │ report_name │ file_path │ file_size  │  │
│  ├─────────────────────────────────────────────────────────┤ │
│  │  1   │    1    │ Lab_Repo... │ uploads/..│   1.2 MB   │  │
│  │  2   │    1    │ X_Ray_Ch... │ uploads/..│   856 KB   │  │
│  │  3   │    2    │ Blood_Te... │ uploads/..│   2.3 MB   │  │
│  └─────────────────────────────────────────────────────────┘  │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

## 🔐 Security Features

### File Validation
```kotlin
// Android Side
- File type validation via MIME types
- Size check before upload

// PHP Side
- Extension whitelist: pdf, jpg, jpeg, png, doc, docx
- Maximum size: 10MB
- File type re-verification
```

### Database Security
```php
// Prepared Statements
$stmt = $conn->prepare("INSERT INTO reports (user_id, ...) VALUES (?, ...)");
$stmt->bind_param("issss", $user_id, ...);

// Foreign Key Constraint
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
```

### File Storage Security
```php
// Unique filenames prevent overwrites
$unique_name = uniqid() . '_' . time() . '.' . $file_ext;

// Files stored outside public web root (uploads/)
// Direct URL access prevented (optional .htaccess rule)
```

## 📈 Performance Considerations

### File Size Optimization
- Maximum 10MB limit prevents server overload
- Base64 encoding increases size by ~33%
- Consider compression for large files

### Database Indexing
```sql
-- Add indexes for faster queries
CREATE INDEX idx_user_id ON reports(user_id);
CREATE INDEX idx_uploaded_at ON reports(uploaded_at);
```

### Cleanup Strategy
```sql
-- Optional: Delete old reports (6 months+)
DELETE FROM reports 
WHERE uploaded_at < DATE_SUB(NOW(), INTERVAL 6 MONTH);

-- Corresponding files must be deleted manually
```

## 🧪 Testing Checklist

### Unit Tests
- [ ] File picker launches correctly
- [ ] File info extracted properly
- [ ] Base64 encoding works
- [ ] API request formatted correctly
- [ ] JSON response parsed correctly

### Integration Tests
- [ ] End-to-end upload works
- [ ] File saved in correct location
- [ ] Database record created
- [ ] Error handling works

### Edge Cases
- [ ] No file selected (shows error)
- [ ] File too large (rejected)
- [ ] Invalid file type (rejected)
- [ ] No internet connection (shows error)
- [ ] User not logged in (handled)
- [ ] Duplicate uploads (unique filenames)

## 📱 UI States

### State 1: Initial (No File)
```
- Upload area visible
- File card hidden
- Submit button enabled but will show error if clicked
```

### State 2: File Selected
```
- Upload area visible
- File card visible with details
- Remove button functional
- Submit button ready
```

### State 3: Uploading
```
- Consider adding: Progress indicator
- Disable submit button
- Show "Uploading..." message
```

### State 4: Success
```
- Show success toast
- Navigate back to Home
- Clear state
```

### State 5: Error
```
- Show error message
- Keep file selected
- Allow retry
```

## 🚀 Future Enhancements

### Phase 2 Features
1. **Multiple File Upload**
   - Select multiple reports at once
   - Batch upload capability

2. **Progress Indicator**
   - Show upload percentage
   - Cancel upload option

3. **Reports List Screen**
   - View all uploaded reports
   - Download/share functionality
   - Delete from app

4. **Image Preview**
   - Show thumbnail for images
   - PDF preview for PDF files

5. **Cloud Storage**
   - Integrate with AWS S3 or Google Cloud
   - Better scalability

6. **Compression**
   - Automatic image compression
   - PDF size reduction

### Phase 3 Features
1. **OCR Integration**
   - Extract text from reports
   - Make reports searchable

2. **Report Categories**
   - Lab reports, X-rays, Prescriptions, etc.
   - Filter by category

3. **Doctor Access**
   - Share reports with specific doctors
   - Access control

4. **Notifications**
   - Upload success notification
   - Reminder to upload reports before appointment

---

**Note**: This flow documentation provides a complete technical overview of the Upload Reports feature implementation.
