<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <title>iMovies | Admin</title>

    <link rel="stylesheet" href="../css/bootstrap.min.css" />
    <link rel="stylesheet" href="../css/style.css" />

</head>
<body>

<div th:replace="fragments/header :: header"></div>

<div class="container">

    asdfasdfadsfa
    <div class="row">
        <h1>Admin Page</h1>
        <div class="section-manage">
            <h3>Number of issued certificates: ${counter}/></h3>
        </div>
        <div class="section-manage">
            <h3>Number of revoked certificates: </h3>
        </div>
        <div class="section-manage">
            <h3>Current serial number: </h3>
        </div>
    </div>


</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script type="text/javascript" src="js/bootstrap.min.js"></script>

</body>
</html>