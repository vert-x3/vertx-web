import {createRoot} from 'react-dom/client';
import 'graphiql/setup-workers/vite';
import App from './App';

const container = document.getElementById('graphiql');
const root = createRoot(container);
root.render(<App/>);
