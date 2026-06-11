import axios from 'axios';

const API_URL = 'http://localhost:8080/api/orders'; // 后端服务的地址

export const createOrder = async (order) => {
  try {
    const response = await axios.post(API_URL, order);
    return response.data;
  } catch (error) {
    console.error('Error creating order:', error);
    throw error;
  }
};

export const getAllOrders = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error('Error fetching orders:', error);
    throw error;
  }
};