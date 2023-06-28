# Image exif data system 2

> This project is second version of already created one with `NodeJS`, you can check it [here](https://github.com/yosko99/img-exif-data-system)

Task of project </br>

- Allows clients to upload/delete (JPEG/TIFF) images through a REST API or GUI.
- Can be queried to return all images inside a geographical bounding box, that is defined by min and max latitude/longitude.
- Can be queried to return the original image and thumbnail (256x256) version of it.
- Has GUI for displaying the images, deleting image and uploading image.

## Used tools

Database - `postgresql` </br>
Backend - `Spring-boot`

## Instructions (with docker 🐋)

Run `docker-compose up` in the main directory. This command will spin up the database and the server for you. </br>

You can access the API under `http://localhost:8080/api/images/` or use the GUI, by opening `index.html` in `/frontend` folder.

## User interface 
![image](https://github.com/yosko99/img-exif-data-system-2/assets/80975936/1bf257ab-0cfb-4cf2-8e4f-130719a2e5a4)

