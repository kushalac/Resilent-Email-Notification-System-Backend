<!DOCTYPE html>
<html>
<head>
    <title>Account Deletion</title>
    <style>
        /* Add your CSS styles here */
        body {
            font-family: Arial, sans-serif;
            background-color: #f8f8f8;
            margin: 0;
            padding: 0;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
            padding: 20px;
            background-color: #ffffff;
            border-radius: 10px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
        }

        h1 {
            color: #333;
            text-align: center;
            font-size: 28px;
            margin-bottom: 20px;
        }

        p {
            color: #666;
            font-size: 16px;
            line-height: 1.5;
        }

        .header {
            background-color: #dc3545;
            color: #fff;
            text-align: center;
            padding: 15px 0;
            border-top-left-radius: 10px;
            border-top-right-radius: 10px;
        }

        .confirmation-button {
            display: inline-block;
            padding: 10px 20px;
            background-color: #007bff;
            color: #fff;
            text-decoration: none;
            border-radius: 5px;
            margin-top: 10px;
        }

        .confirmation-button:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Account Deletion</h1>
        </div>
        <p>Hello <span style="font-weight: bold;">${name}</span>,</p>
        <p>Your account deletion process has been initiated.</p>
        <p>Please click on the following button to confirm the deletion:</p>
        <a href="${confirmationLink}" class="confirmation-button">Confirm Deletion</a>
        <p>If you have any questions or need assistance, please don't hesitate to reach out to our support team</p>
    </div>
</body>
</html>
