import os
import shutil

# Root folder where docker-compose looks
root_dir = "C:/Users/Nick/Desktop/MiniGame"
base_path = os.path.join(root_dir, "sample-music")
real_music_source = os.path.join(root_dir, "archive/fma_medium/000/000005.mp3")

# Clean out any old/bad data first
if os.path.exists(base_path):
    shutil.rmtree(base_path)
os.makedirs(base_path)

data = {
    "Daft Punk": {
        "Discovery": [
            "One More Time", "Aerodynamic", "Digital Love", "Harder Better Faster Stronger",
            "Crescendolls", "Nightvision", "Superheroes", "High Life", "Something About Us", "Voyager"
        ],
        "Random Access Memories": [
            "Give Life Back to Music", "The Game of Love", "Giorgio by Moroder", "Within",
            "Instant Crush", "Lose Yourself to Dance", "Touch", "Get Lucky", "Beyond", "Motherboard"
        ]
    },
    "Pink Floyd": {
        "The Dark Side of the Moon": [
            "Speak to Me", "Breathe", "On the Run", "Time", "The Great Gig in the Sky",
            "Money", "Us and Them", "Any Colour You Like", "Brain Damage", "Eclipse"
        ]
    },
    "Billie Eilish": {
        "WHEN WE ALL FALL ASLEEP": [
            "bad guy", "xanny", "you should see me in a crown", "all the good girls go to hell",
            "wish you were gay", "when the party's over", "8", "my strange addiction", "bury a friend", "ilomilo"
        ],
        "Hit Me Hard and Soft": [
            "SKINNY", "LUNCH", "CHIHIRO", "BIRDS OF A FEATHER", "WILDFLOWER",
            "THE GREATEST", "LAMOUR DE MA VIE", "THE DINER", "BITTERSUITE", "BLUE"
        ]
    }
}

with open(real_music_source, 'rb') as f:
    audio_data = f.read()

for artist, albums in data.items():
    for album, songs in albums.items():
        album_dir = os.path.join(base_path, artist, album)
        os.makedirs(album_dir, exist_ok=True)
        for song in songs:
            file_path = os.path.join(album_dir, f"{song}.mp3")
            with open(file_path, 'wb') as f:
                f.write(audio_data)

print(f"Refreshed 50 songs in {base_path}")
