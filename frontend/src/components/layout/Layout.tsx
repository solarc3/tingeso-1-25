import { Outlet } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';
import { Toaster } from '@/components/ui/sonner';

export default function Layout() {
    return (
        <div className="min-h-screen flex flex-col">
            <Header />
            <main className="flex-grow px-4 sm:px-6 md:px-8 py-4 sm:py-6">
                <Outlet />
            </main>
            <Footer />
            <Toaster />
        </div>
    );
}