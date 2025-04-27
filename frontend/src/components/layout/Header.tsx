import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';

export default function Header() {
    return (
        <header className="bg-background border-b">
            <div className="container mx-auto px-4 py-4 flex items-center justify-between">
                <Link to="/" className="font-bold text-2xl">KartingRM</Link>

                <nav className="flex items-center gap-4">
                    <Link to="/" className="text-foreground hover:text-primary transition-colors">
                        Inicio
                    </Link>
                    <Button asChild>
                        <Link to="/booking">Reservar Ahora</Link>
                    </Button>
                </nav>
            </div>
        </header>
    );
}