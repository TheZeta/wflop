# WFLOP

**Wind Farm Layout Optimization Platform (WFLOP)** is a modular Java framework for modeling, analyzing, and benchmarking wind farm layouts. It supports flexible configuration loading, precomputed wake effects, and JMH-powered performance benchmarks.

## Modules

- **`wflop-model`** – Defines core data structures (e.g., `WFLOP`, `WindProfile`, `Solution`).
- **`wflop-core`** – Contains calculation components like wake and power output models.
- **`wflop-config`** – Responsible for loading model instances (e.g., from JSON files).
- **`wflop-benchmark`** – Benchmarks computational performance using [JMH](https://openjdk.org/projects/code-tools/jmh/).
- **`wflop-testdata`** – Contains shared configuration files for object instantiation.
- **`scripts`** – Contains helper scripts (e.g., for visualizing benchmark results).
- **`benchmark-results`** – Stores output data and plots generated from benchmark runs.

## Features

- Support for precomputed distance and intersection matrices
- Plug-and-play wind profile input via config module
- JSON-driven instantiation of complex objects
- Isolated benchmarking support with JMH
- Integrated Python visualization script for benchmark results
- Clean separation of concerns across modules

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/TheZeta/wflop.git
   cd wflop
   
2. Build the project:
    ```bash
   mvn clean install
   
3. Run benchmarks:
    ```bash
   cd wflop-benchmark
   mvn clean install
   java -jar target/benchmarks
   
4. Visualize benchmark results:
    ```bash
   cd scripts
   python plot_benchmarks.py

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
    