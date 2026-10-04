package com.example.data.repository

import com.example.data.model.Song

object SongRepository {
    val songs: List<Song> = listOf(
        Song(
            id = 0,
            title = "A Bar Song (Tipsy)",
            artist = "Shaboozey",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/72/53/c7/7253c7f2-b61f-baf3-3b4e-08171e35cfea/mzaf_921634286400386475.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/54/50/75/54507577-9e1d-c2c0-79dd-81e65aa7fb9d/197342550925_cover.jpg/400x400bb.jpg",
            artworkBgColor = "#8B4513",
            lyrics = listOf(
                "My baby want a Birkin, she's been telling me all night long",
                "Gasoline and groceries, the list goes on and on",
                "This 9 to 5 ain't working, why the hell do I work so hard?",
                "Can't remember when I made it home before the dark",
                "",
                "Someone pour me up a double shot of whiskey",
                "They know me and Jack Daniels got a history",
                "There's a party downtown near 5th Street",
                "Everybody at the bar getting tipsy",
                "",
                "One, here comes the two, to the three, to the four",
                "Tell 'em 'Bring another round', we need plenty more",
                "Two steppin' on the table, double fisting on the floor",
                "Everybody at the bar getting tipsy"
            )
        ),
        Song(
            id = 2,
            title = "Birds Of A Feather",
            artist = "Billie Eilish",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/ca/08/ce/ca08ce69-fa1f-e9bf-16da-dfd34aa80b49/mzaf_4558229648643978920.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/92/9f/69/929f69f1-9977-3a44-d674-11f70c852d1b/24UMGIM36186.rgb.jpg/400x400bb.jpg",
            artworkBgColor = "#2F4F4F",
            lyrics = listOf(
                "I want you to stay",
                "'Til I'm in the grave",
                "'Til I rot away, dead and buried",
                "'Til I'm in the casket you carry",
                "",
                "If you go, I'm going too, uh",
                "'Cause it was always you, alright",
                "And if I'm turning blue, please don't save me",
                "Nothing left to lose without my baby",
                "",
                "Birds of a feather, we should stick together, I know",
                "I said I'd never think I wasn't better alone",
                "Can't change the weather, might not be forever",
                "But if it's forever, it's even better"
            )
        ),
        Song(
            id = 3,
            title = "Espresso",
            artist = "Sabrina Carpenter",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/97/a2/b8/97a2b874-e3de-5658-f889-5d0eedb72d3f/mzaf_13627043593740003520.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/cb/64/8c/cb648cf7-e7bb-bd00-efe0-d744312c29a8/24UMGIM32304.rgb.jpg/400x400bb.jpg",
            artworkBgColor = "#CD853F",
            lyrics = listOf(
                "Now he's thinkin' 'bout me every night, oh",
                "Is it that sweet? I guess so",
                "Say you can't sleep, baby, I know",
                "That's that me espresso",
                "",
                "Move it up, down, left, right, oh",
                "Switch it up like Nintendo",
                "Say you can't sleep, baby, I know",
                "That's that me espresso",
                "",
                "I can't relate to desperation",
                "My give-a-f***s are on vacation",
                "And I got this one boy and he won't stop calling",
                "When they act this way, I know I got 'em"
            )
        ),
        Song(
            id = 4,
            title = "Die With A Smile",
            artist = "Lady Gaga & Bruno Mars",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/11/ae/f2/11aef294-f57c-bab9-c9fc-529162984e62/24UMGIM85348.rgb.jpg/400x400bb.jpg",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/11/ae/f2/11aef294-f57c-bab9-c9fc-529162984e62/24UMGIM85348.rgb.jpg/400x400bb.jpg",
            artworkBgColor = "#800080",
            lyrics = listOf(
                "Ooh, if the world was ending",
                "I'd wanna be next to you",
                "If the party was over",
                "And our time on Earth was through",
                "",
                "I'd wanna hold you just for a while",
                "And die with a smile",
                "If the world was ending",
                "I'd wanna be next to you",
                "",
                "Right next to you"
            )
        ),
        Song(
            id = 5,
            title = "I Had Some Help",
            artist = "Post Malone & Morgan Wallen",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/c4/fa/e2/c4fae20a-494c-e06f-99e3-b6e820f8cc87/mzaf_7882276521788841354.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/e1/b7/09/e1b7098f-d6e1-2b18-fd6f-8390110908eb/24UMGIM50612.rgb.jpg/400x400bb.jpg",
            artworkBgColor = "#4B0082",
            lyrics = listOf(
                "You thought I'd take the blame",
                "Like you did nothing wrong",
                "I had some help getting to this state",
                "It takes two to make a mess like this"
            )
        ),
        Song(
            id = 6,
            title = "Lose Control",
            artist = "Teddy Swims",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview116/v4/3a/d0/30/3ad03076-a30e-9a8d-46bf-3cf3e36e31c9/mzaf_15630973341141916680.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music126/v4/36/19/66/36196640-1561-dc5e-c6bc-1e5f4befa583/093624856771.jpg/400x400bb.jpg",
            artworkBgColor = "#FF4500",
            lyrics = listOf(
                "Something's in the orange",
                "I lose control when you're not next to me",
                "I'm falling apart at the seams",
                "Baby, I lose control"
            )
        ),
        Song(
            id = 7,
            title = "Good Luck, Babe!",
            artist = "Chappell Roan",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/ae/57/5d/ae575db4-68db-508a-c012-421ee79bfa62/mzaf_5112451481032254728.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/29/a7/c4/29a7c478-351d-25eb-a116-3e68118cdab8/24UMGIM31246.rgb.jpg/400x400bb.jpg",
            artworkBgColor = "#A55F47",
            lyrics = listOf(
                "You'd have to stop the world just to stop the feeling",
                "Good luck, babe, you'd have to stop the world",
                "When you wake up next to him in the middle of the night",
                "With your head in your hands, you're nothing more than his wife"
            )
        ),
        Song(
            id = 8,
            title = "Taste",
            artist = "Sabrina Carpenter",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview112/v4/bb/6b/c8/bb6bc802-78d2-c0b9-1434-cdcd7c6c7eb3/mzaf_5610996391320604947.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music115/v4/a5/c4/24/a5c424a5-8084-47ab-3fa8-e82f00faf1bf/888915632314_cover.jpg/400x400bb.jpg",
            artworkBgColor = "#FF1493",
            lyrics = listOf(
                "Oh, I leave quite an impression",
                "Five feet to be exact",
                "You're wonderin' why half his clothes went missin'",
                "My body's where they're at"
            )
        ),
        Song(
            id = 9,
            title = "Beautiful Things",
            artist = "Benson Boone",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview126/v4/2d/4e/7b/2d4e7b94-5521-568f-f269-c8643001d32b/mzaf_6034909346296341668.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music116/v4/54/f4/92/54f49210-e260-b519-ebbd-f4f40ee710cd/054391342751.jpg/400x400bb.jpg",
            artworkBgColor = "#4682B4",
            lyrics = listOf(
                "For a while there it was rough",
                "But lately, I've been doing better",
                "Please stay, I want you, I need you, oh God",
                "Don't take these beautiful things that I've got"
            )
        ),
        Song(
            id = 10,
            title = "Please Please Please",
            artist = "Sabrina Carpenter",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/78/6b/15/786b15ad-8958-8019-28e2-a29f1c64f9d4/mzaf_9652925359746163912.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/f6/97/58/f69758d6-24d8-6e44-be5b-26819921bfc7/24UMGIM61704.rgb.jpg/400x400bb.jpg",
            artworkBgColor = "#DA70D6",
            lyrics = listOf(
                "Please, please, please",
                "Don't prove 'em right",
                "And please, please, please",
                "Don't bring me to tears when I just did my makeup so nice"
            )
        ),
        Song(
            id = 12,
            title = "Not Like Us",
            artist = "Kendrick Lamar",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/df/05/e7/df05e74c-c4a1-b6ca-61b0-e2c36df3ebfd/mzaf_9714719170473697657.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/d0/ef/b6/d0efb685-73be-fdee-58c9-be655f4cd4fd/24UMGIM51924.rgb.jpg/400x400bb.jpg",
            artworkBgColor = "#483D8B",
            lyrics = listOf(
                "Psst, I see dead people",
                "Mustard on the beat, ho",
                "They not like us, they not like us, they not like us",
                "Sometimes you gotta pop out and show"
            )
        ),
        Song(
            id = 13,
            title = "Too Sweet",
            artist = "Hozier",
            mp4Link = "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview211/v4/58/77/2b/58772b8f-f76e-faaf-31dd-db5d8c57376e/mzaf_7312578909270296724.plus.aac.p.m4a",
            artworkUrl = "https://is1-ssl.mzstatic.com/image/thumb/Music221/v4/75/43/b9/7543b9da-b57f-4378-7eaf-1e02b49f5cc5/24UMGIM21983.rgb.jpg/400x400bb.jpg",
            artworkBgColor = "#8B4513",
            lyrics = listOf(
                "I take my whiskey neat, my coffee black and my bed at three",
                "You're too sweet for me",
                "You're too sweet for me"
            )
        )
    )

    fun getSongById(id: Int): Song? = songs.find { it.id == id }

    fun searchSongs(query: String): List<Song> {
        if (query.isBlank()) return songs
        return songs.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true)
        }
    }
}
