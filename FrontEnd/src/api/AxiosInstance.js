import axios from 'axios'

const AxiosInstance = axios.create({
    baseURL:'http://localhost:8081'
}) ;
export default AxiosInstance;