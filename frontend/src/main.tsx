import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import './index.css';
import Layout from './components/layout/Layout';
import HomePage from './pages/HomePage';
import BookingPage from './pages/BookingPage';
import ConfirmationPage from './pages/ConfirmationPage';

createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Layout />}>
                    <Route index element={<HomePage />} />
                    <Route path="booking" element={<BookingPage />} />
                    <Route path="confirmation" element={<ConfirmationPage />} />
                </Route>
            </Routes>
        </BrowserRouter>
    </StrictMode>
);